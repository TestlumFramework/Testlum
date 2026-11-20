const http = require('http');
const { Pool } = require('pg');

const pool = new Pool({ connectionString: process.env.DATABASE_URL || 'postgres://storyboard:storyboard@localhost:5432/storyboard' });
const STATUSES = ['todo', 'doing', 'done'];
const COLUMNS = 'id, title, status, points, assignee';

function send(res, code, body) {
  if (body === undefined) {
    res.writeHead(code).end();
    return;
  }
  res.writeHead(code, { 'Content-Type': 'application/json' }).end(JSON.stringify(body));
}

function readJson(req) {
  return new Promise((resolve, reject) => {
    let raw = '';
    req.on('data', c => (raw += c));
    req.on('end', () => {
      try {
        resolve(raw ? JSON.parse(raw) : {});
      } catch (e) {
        reject(new Error('body is not valid JSON'));
      }
    });
  });
}

async function route(req, res) {
  const url = new URL(req.url, 'http://x');
  const m = url.pathname.match(/^\/api\/stories(?:\/(\d+))?$/);

  if (req.method === 'GET' && url.pathname === '/api/health') {
    await pool.query('select 1');
    return send(res, 200, { status: 'ok' });
  }
  if (!m) return send(res, 404, { error: 'Not found' });
  const id = m[1] && Number(m[1]);

  if (req.method === 'GET' && !id) {
    const status = url.searchParams.get('status');
    const assignee = process.env.SB14 ? url.searchParams.get('assignee') : null;
    if (status && !STATUSES.includes(status)) return send(res, 400, { error: 'status must be todo, doing or done' });
    const where = [], args = [];
    if (status) { args.push(status); where.push(`status = $${args.length}`); }
    if (assignee) { args.push(assignee); where.push(`lower(assignee) = lower($${args.length})`); }
    const r = await pool.query(`select ${COLUMNS} from stories ${where.length ? 'where ' + where.join(' and ') : ''} order by id`, args);
    return send(res, 200, r.rows);
  }
  if (req.method === 'GET') {
    const r = await pool.query(`select ${COLUMNS} from stories where id = $1`, [id]);
    return r.rows[0] ? send(res, 200, r.rows[0]) : send(res, 404, { error: 'Story not found' });
  }
  if (req.method === 'POST' && !id) {
    const b = await readJson(req);
    if (typeof b.title !== 'string' || !b.title.trim()) return send(res, 400, { error: 'title is required' });
    const points = b.points === undefined ? 1 : b.points;
    if (!Number.isInteger(points) || points < 1 || points > 13) return send(res, 400, { error: 'points must be between 1 and 13' });
    const r = await pool.query(`insert into stories (title, points, assignee) values ($1, $2, $3) returning ${COLUMNS}`, [b.title.trim(), points, b.assignee || null]);
    return send(res, 201, r.rows[0]);
  }
  if (req.method === 'PATCH' && id) {
    const b = await readJson(req);
    if (!STATUSES.includes(b.status)) return send(res, 400, { error: 'status must be todo, doing or done' });
    const r = await pool.query(`update stories set status = $2 where id = $1 returning ${COLUMNS}`, [id, b.status]);
    return r.rows[0] ? send(res, 200, r.rows[0]) : send(res, 404, { error: 'Story not found' });
  }
  if (req.method === 'DELETE' && id) {
    const r = await pool.query('delete from stories where id = $1', [id]);
    return r.rowCount ? send(res, 204) : send(res, 404, { error: 'Story not found' });
  }
  return send(res, 405, { error: 'Method not allowed' });
}

http.createServer((req, res) => {
  route(req, res).catch(e => send(res, e.message === 'body is not valid JSON' ? 400 : 500, { error: e.message }));
}).listen(process.env.PORT || 8080, () => console.log('storyboard on', process.env.PORT || 8080));

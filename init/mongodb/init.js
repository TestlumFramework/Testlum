db.auth('username', 'password');
db = db.getSiblingDB('database');
db.createUser(
    {
        user: "playground",
        pwd: "root",
        roles: [
            {
                role: "readWrite",
                db: "playground_db"
            }
        ]
    }
);
INSERT INTO t_email_template (id, name, title, html, created_at)
VALUES (1, 'EMAIL_VERIFICATION', 'Email verification', '<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <title>Simple Transactional Email</title>
    <style>
.btn-primary table td:hover {
  background-color: transparent !important;
}
.btn-primary a:hover {
  background-color: transparent !important;
  border-color: #0FC7D2 !important;
  color: #0FC7D2 !important;
}
@media only screen and (max-width: 620px) {
  table[class=body] h1 {
    font-size: 28px !important;
    margin-bottom: 10px !important;
  }

  table[class=body] p,
table[class=body] ul,
table[class=body] ol,
table[class=body] td,
table[class=body] span,
table[class=body] a {
    font-size: 16px !important;
  }

  table[class=body] .wrapper,
table[class=body] .article {
    padding: 10px !important;
  }

  table[class=body] .content {
    padding: 0 !important;
  }

  table[class=body] .container {
    padding: 0 !important;
    width: 100% !important;
  }

  table[class=body] .main {
    border-left-width: 0 !important;
    border-radius: 0 !important;
    border-right-width: 0 !important;
  }

  table[class=body] .btn table {
    width: 100% !important;
  }

  table[class=body] .btn a {
    width: 100% !important;
  }

  table[class=body] .img-responsive {
    height: auto !important;
    max-width: 100% !important;
    width: auto !important;
  }
}
@media all {
  .ExternalClass {
    width: 100%;
  }

  .ExternalClass,
.ExternalClass p,
.ExternalClass span,
.ExternalClass font,
.ExternalClass td,
.ExternalClass div {
    line-height: 100%;
  }

  .apple-link a {
    color: inherit !important;
    font-family: inherit !important;
    font-size: inherit !important;
    font-weight: inherit !important;
    line-height: inherit !important;
    text-decoration: none !important;
  }

  #MessageViewBody a {
    color: inherit;
    text-decoration: none;
    font-size: inherit;
    font-family: inherit;
    font-weight: inherit;
    line-height: inherit;
  }
}
</style>
</head>
<body class style="background-color: #f6f6f6; font-family: ''Inter'', sans-serif; -webkit-font-smoothing: antialiased; font-size: 14px; line-height: 1.4; margin: 0; padding: 0; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;">
<span class="preheader" style="color: transparent; display: none; height: 0; max-height: 0; max-width: 0; opacity: 0; overflow: hidden; mso-hide: all; visibility: hidden; width: 0;">This is preheader text. Some clients will show this text as a preview.</span>
<table role="presentation" border="0" cellpadding="0" cellspacing="0" class="body" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #f6f6f6; width: 100%;" width="100%" bgcolor="#f6f6f6">
    <tr>
        <td style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top;" valign="top">&nbsp;</td>
        <td class="container" style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top; display: block; max-width: 580px; padding: 10px; width: 100%; margin: 0 auto;" width="100%" valign="top">
            <div class="content" style="box-sizing: border-box; display: block; margin: 0 auto; max-width: 580px; width: 100%; padding: 10px;">

                <!-- START CENTERED WHITE CONTAINER -->
                <table role="presentation" class="main" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background: #ffffff; border-radius: 3px; width: 100%;" width="100%">

                    <!-- START MAIN CONTENT AREA -->
                    <tr>
                        <td class="wrapper" style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top; box-sizing: border-box; padding: 50px 40px; background-size: cover; background-position: 50% 50%; background-repeat: no-repeat; background-image: url(https://djooky-dev.s3.eu-central-1.amazonaws.com/2021-09-06+2.53.05+PM.jpg);" background="https://djooky-dev.s3.eu-central-1.amazonaws.com/2021-09-06+2.53.05+PM.jpg" valign="top">
                            <table class="main-content" role="presentation" border="0" cellpadding="0" cellspacing="0" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%; background: rgba(255, 255, 255, 0.7); padding: 50px 20px; border-radius: 25px;" width="100%">
                                <tr>
                                    <td style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top;" valign="top">
                                        <p style="font-family: ''Inter'', sans-serif; font-size: 16px; font-weight: 500; margin: 0; margin-bottom: 15px; text-align: center; color: #333;"><img src="https://djooky-dev.s3.eu-central-1.amazonaws.com/2021-09-06+2.53.11+PM.jpg" width="125px" height="40px" alt="logo" style="border: none; -ms-interpolation-mode: bicubic; max-width: 100%;"></p>
                                        <p style="font-family: ''Inter'', sans-serif; font-size: 16px; font-weight: 500; margin: 0; margin-bottom: 15px; text-align: center; color: #333;">We''re glad you''re here,</p>
                                        <p style="font-family: ''Inter'', sans-serif; font-size: 16px; font-weight: 500; margin: 0; margin-bottom: 15px; text-align: center; color: #333;"><a href="http://htmlemail.io" target="_blank" style="color: #0FC7D2; text-decoration: underline; font-weight: 600;">${email}</a> in PME.</p>
                                        <table class="code-wrapper" role="presentation" border="0" cellpadding="0" cellspacing="0" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%; margin-top: 100px;" width="100%">
                                            <tbody>
                                            <tr>
                                                <td style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top;" valign="top">
                                                    <p style="font-family: ''Inter'', sans-serif; font-size: 16px; font-weight: 500; margin: 0; margin-bottom: 15px; text-align: center; color: #333;">Copy your unique access code</p>
                                                    <p class="code-content" style="font-family: ''Inter'', sans-serif; margin: 0; margin-bottom: 15px; text-align: center; font-weight: 600; font-size: 30px; line-height: 36px; background: rgba(28, 202, 205, 0.05); border-radius: 5px; padding: 20px; color: #333;">${token}</p>
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                        <p style="font-family: ''Inter'', sans-serif; font-size: 16px; font-weight: 500; margin: 0; margin-bottom: 15px; text-align: center; color: #333;">and</p>
                                        <table style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; box-sizing: border-box; width: 100%;" role="presentation" border="0" cellpadding="0" cellspacing="0" class="btn btn-primary" width="100%">
                                            <tbody style="width: 100%;">
                                            <tr style="width: 100%;">
                                                <td align="left" style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top; padding-bottom: 15px; width: 100%;" width="100%" valign="top">
                                                    <table style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
                                                        <tbody style="width: 100%;">
                                                        <tr style="width: 100%;">
                                                            <a href="${url}" target="_blank" style="border: solid 1px #3498db; border-radius: 25px; box-sizing: border-box; cursor: pointer; font-size: 16px; font-weight: 500; margin: 0; padding: 15px 15px; text-decoration: none; text-transform: capitalize; background-color: #0FC7D2; border-color: #0FC7D2; color: #ffffff; text-align: center; transition: all .5s; display: block; width: 100%;">Activate Account</a>
                                                        </tr>
                                                        </tbody>
                                                    </table>
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <!-- END MAIN CONTENT AREA -->
                </table>
                <!-- END CENTERED WHITE CONTAINER -->

                <!-- START FOOTER -->
<!--                <div class="footer">-->
<!--                    <table role="presentation" border="0" cellpadding="0" cellspacing="0">-->
<!--                        <tr>-->
<!--                            <td class="content-block">-->
<!--                                <span class="apple-link">Company Inc, 3 Abbey Road, San Francisco CA 94102</span>-->
<!--                                <br> Don''t like these emails? <a href="">Unsubscribe</a>.-->
<!--                            </td>-->
<!--                        </tr>-->
<!--                        <tr>-->
<!--                            <td class="content-block powered-by">-->
<!--                                Powered by <a href="http://htmlemail.io">HTMLemail</a>.-->
<!--                            </td>-->
<!--                        </tr>-->
<!--                    </table>-->
<!--                </div>-->
                <!-- END FOOTER -->

            </div>
        </td>
        <td style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top;" valign="top">&nbsp;</td>
    </tr>
</table>
</body>
</html>', current_date + 1),
       (2, 'PASSWORD_RECOVERY', 'Password recovery', '<!doctype html>
<html>
<head>
    <meta name="viewport" content="width=device-width">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <title>Simple Transactional Email</title>
    <style>
.btn-primary table td:hover {
  background-color: transparent !important;
}
.btn-primary a:hover {
  background-color: transparent !important;
  border-color: #0FC7D2 !important;
  color: #0FC7D2 !important;
}
@media only screen and (max-width: 620px) {
  table[class=body] h1 {
    font-size: 28px !important;
    margin-bottom: 10px !important;
  }

  table[class=body] p,
table[class=body] ul,
table[class=body] ol,
table[class=body] td,
table[class=body] span,
table[class=body] a {
    font-size: 16px !important;
  }

  table[class=body] .wrapper,
table[class=body] .article {
    padding: 10px !important;
  }

  table[class=body] .content {
    padding: 0 !important;
  }

  table[class=body] .container {
    padding: 0 !important;
    width: 100% !important;
  }

  table[class=body] .main {
    border-left-width: 0 !important;
    border-radius: 0 !important;
    border-right-width: 0 !important;
  }

  table[class=body] .btn table {
    width: 100% !important;
  }

  table[class=body] .btn a {
    width: 100% !important;
  }

  table[class=body] .img-responsive {
    height: auto !important;
    max-width: 100% !important;
    width: auto !important;
  }
}
@media all {
  .ExternalClass {
    width: 100%;
  }

  .ExternalClass,
.ExternalClass p,
.ExternalClass span,
.ExternalClass font,
.ExternalClass td,
.ExternalClass div {
    line-height: 100%;
  }

  .apple-link a {
    color: inherit !important;
    font-family: inherit !important;
    font-size: inherit !important;
    font-weight: inherit !important;
    line-height: inherit !important;
    text-decoration: none !important;
  }

  #MessageViewBody a {
    color: inherit;
    text-decoration: none;
    font-size: inherit;
    font-family: inherit;
    font-weight: inherit;
    line-height: inherit;
  }
}
</style>
</head>
<body class style="background-color: #f6f6f6; font-family: ''Inter'', sans-serif; -webkit-font-smoothing: antialiased; font-size: 14px; line-height: 1.4; margin: 0; padding: 0; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;">
<span class="preheader" style="color: transparent; display: none; height: 0; max-height: 0; max-width: 0; opacity: 0; overflow: hidden; mso-hide: all; visibility: hidden; width: 0;">This is preheader text. Some clients will show this text as a preview.</span>
<table role="presentation" border="0" cellpadding="0" cellspacing="0" class="body" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #f6f6f6; width: 100%;" width="100%" bgcolor="#f6f6f6">
    <tr>
        <td style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top;" valign="top">&nbsp;</td>
        <td class="container" style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top; display: block; max-width: 580px; padding: 10px; width: 100%; margin: 0 auto;" width="100%" valign="top">
            <div class="content" style="box-sizing: border-box; display: block; margin: 0 auto; max-width: 580px; width: 100%; padding: 10px;">

                <!-- START CENTERED WHITE CONTAINER -->
                <table role="presentation" class="main" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background: #ffffff; border-radius: 3px; width: 100%;" width="100%">

                    <!-- START MAIN CONTENT AREA -->
                    <tr>
                        <td class="wrapper" style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top; box-sizing: border-box; padding: 50px 40px; background-size: cover; background-position: 50% 50%; background-repeat: no-repeat; background-image: url(https://djooky-dev.s3.eu-central-1.amazonaws.com/2021-09-06+2.53.05+PM.jpg);" background="https://djooky-dev.s3.eu-central-1.amazonaws.com/2021-09-06+2.53.05+PM.jpg" valign="top">
                            <table class="main-content" role="presentation" border="0" cellpadding="0" cellspacing="0" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%; background: rgba(255, 255, 255, 0.7); padding: 50px 20px; border-radius: 25px;" width="100%">
                                <tr>
                                    <td style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top;" valign="top">
                                        <p style="font-family: ''Inter'', sans-serif; font-size: 16px; font-weight: 500; margin: 0; margin-bottom: 15px; text-align: center; color: #333;"><img src="https://djooky-dev.s3.eu-central-1.amazonaws.com/2021-09-06+2.53.11+PM.jpg" width="125px" height="40px" alt="logo" style="border: none; -ms-interpolation-mode: bicubic; max-width: 100%;"></p>
                                        <p style="font-family: ''Inter'', sans-serif; font-size: 16px; font-weight: 500; margin: 0; margin-bottom: 15px; text-align: center; color: #333;">We''re glad you''re here,</p>
                                        <p style="font-family: ''Inter'', sans-serif; font-size: 16px; font-weight: 500; margin: 0; margin-bottom: 15px; text-align: center; color: #333;"><a href="http://htmlemail.io" target="_blank" style="color: #0FC7D2; text-decoration: underline; font-weight: 600;">${email}</a> in PME.</p>
                                        <table class="code-wrapper" role="presentation" border="0" cellpadding="0" cellspacing="0" style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%; margin-top: 100px;" width="100%">
                                            <tbody>
                                            <tr>
                                                <td style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top;" valign="top">
                                                    <p style="font-family: ''Inter'', sans-serif; font-size: 16px; font-weight: 500; margin: 0; margin-bottom: 15px; text-align: center; color: #333;">Copy your unique access code</p>
                                                    <p class="code-content" style="font-family: ''Inter'', sans-serif; margin: 0; margin-bottom: 15px; text-align: center; font-weight: 600; font-size: 30px; line-height: 36px; background: rgba(28, 202, 205, 0.05); border-radius: 5px; padding: 20px; color: #333;">${token}</p>
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                        <p style="font-family: ''Inter'', sans-serif; font-size: 16px; font-weight: 500; margin: 0; margin-bottom: 15px; text-align: center; color: #333;">and</p>
                                        <table style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; box-sizing: border-box; width: 100%;" role="presentation" border="0" cellpadding="0" cellspacing="0" class="btn btn-primary" width="100%">
                                            <tbody style="width: 100%;">
                                            <tr style="width: 100%;">
                                                <td align="left" style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top; padding-bottom: 15px; width: 100%;" width="100%" valign="top">
                                                    <table style="border-collapse: separate; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%">
                                                        <tbody style="width: 100%;">
                                                        <tr style="width: 100%;">
                                                            <a href="${url}" target="_blank" style="border: solid 1px #3498db; border-radius: 25px; box-sizing: border-box; cursor: pointer; font-size: 16px; font-weight: 500; margin: 0; padding: 15px 15px; text-decoration: none; text-transform: capitalize; background-color: #0FC7D2; border-color: #0FC7D2; color: #ffffff; text-align: center; transition: all .5s; display: block; width: 100%;">Reset Password</a>
                                                        </tr>
                                                        </tbody>
                                                    </table>
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <!-- END MAIN CONTENT AREA -->
                </table>
                <!-- END CENTERED WHITE CONTAINER -->

                <!-- START FOOTER -->
<!--                <div class="footer">-->
<!--                    <table role="presentation" border="0" cellpadding="0" cellspacing="0">-->
<!--                        <tr>-->
<!--                            <td class="content-block">-->
<!--                                <span class="apple-link">Company Inc, 3 Abbey Road, San Francisco CA 94102</span>-->
<!--                                <br> Don''t like these emails? <a href="">Unsubscribe</a>.-->
<!--                            </td>-->
<!--                        </tr>-->
<!--                        <tr>-->
<!--                            <td class="content-block powered-by">-->
<!--                                Powered by <a href="http://htmlemail.io">HTMLemail</a>.-->
<!--                            </td>-->
<!--                        </tr>-->
<!--                    </table>-->
<!--                </div>-->
                <!-- END FOOTER -->

            </div>
        </td>
        <td style="font-family: ''Inter'', sans-serif; font-size: 14px; vertical-align: top;" valign="top">&nbsp;</td>
    </tr>
</table>
</body>
</html>', current_date + 1),
       (3, 'SUPPORT_PAGE', 'Support email',
        '<p>NickName: ${nickName}</p>
        <br>
        <p>Email: ${email}</p>
        <br>
        <p>Subject: ${subject}</p>
        <br>
        <p>Description: ${description}</p>',
        current_date + 1),
       (4, 'LOGIN_2FA', 'Login verification PME',
        '<h1>Code for login ${email}</h1>
         <h2>${code}</h2>',
        now());;

@import gg.jte.Content

@param String title
@param Content header = null
@param Content content

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${title} — КапиЖилСервисЦентр</title>
    <link rel="stylesheet" href="https://unpkg.com/@picocss/pico@latest/css/pico.min.css">
    ${header}
</head>
<body>
    ${content}
</body>
</html>
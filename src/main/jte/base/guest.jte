@import gg.jte.Content

@param String title
@param Content content

@template.base.layout(
    title = title,
    header = @`
    <link rel="stylesheet" href="/assets/login.css" />
    `,
    content = content
)

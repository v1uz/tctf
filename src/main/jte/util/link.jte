@param String active
@param String slug
@param String href
@param String value

<li><a href="${href}">
@if(slug.equals(active))
<strong>${value}</strong>
@else
${value}
@endif
</a></li>

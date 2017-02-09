---
layout: single
title: Blog Posts by Tag
desc: "A list of blog posts organized by tags"
---

Click on a tag to see relevant list of posts.

<ul class="tags">
{% for tag in site.tags %}
  {% assign t = tag | first %}
  <li><a href="/tag/#{{t |  replace:" ","-" }}">{{ t  }}</a></li>
{% endfor %}
</ul>


{% for tag in site.tags %}
  {% assign t = tag | first %}
  {% assign posts = tag | last %}

<h4><a name="{{t | replace:" ","-" }}"></a><a class="internal" href="/tag/#{{t | replace:" ","-" }}">{{ t }}</a></h4>
<ul>
{% for post in posts %}
  {% if post.tags contains t %}
  <li>
    <a href="{{ post.url }}">{{ post.title }}</a>
    <span class="date">{{ post.date | date: "%B %-d, %Y"  }}</span>
  </li>
  {% endif %}
{% endfor %}
</ul>



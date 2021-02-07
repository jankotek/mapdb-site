
    var url = "https://github.com/mapdb/mapdb-site-comments/issues/" + {{ page.ghcommentid }};
    var api_url = "https://api.github.com/repos/mapdb/mapdb-site-comments/issues/" + {{ page.ghcommentid }} + "/comments";

$(document).ready(function () {
    $.ajax(api_url, {
        headers: {Accept: "application/vnd.github.v3.html+json"},
        dataType: "json",
        success: function(comments) {
            $("#page__comments").append("Visit the <b><a href='" + url + "'>Github Issue</a></b> to comment on this post");
            $.each(comments, function(i, comment) {

                var date = new Date(comment.created_at);

                var t = "<div id='gh-comment'>";
                t += "<img src='" + comment.user.avatar_url + "' width='24px'>";
                t += "<b><a href='" + comment.user.html_url + "'>" + comment.user.login + "</a></b>";
                t += " posted at ";
                t += "<em>" + date.toUTCString() + "</em>";
                t += "<div id='gh-comment-hr'></div>";
                t += comment.body_html;
                t += "</div>";
                $("#page__comments").append(t);
            });
        },
        error: function() {
            $("#page__comments").append("Comments are not open for this post yet.");
        }
    });
});

 // Roland Yonaba - 2012
 // Base code (http://aboutcode.net/2010/11/11/list-github-projects-using-javascript.html)
 // Fix for compatibility with Github API v3 (http://developer.github.com/v3/)
 // Fix getJSOn link request for compatibility with Github API v3 (thanks to MJoyce : http://stackoverflow.com/questions/11850527/use-javascript-to-get-the-list-of-a-users-github-repositories)
 
     
    jQuery.githubUser = function(username, callback) {
	  jQuery.getJSON('https://api.github.com/users/'+username+'/repos?callback=?',callback)
    }

    jQuery.fn.loadRepositories = function(username, keyword) {
      //this.html("<span>Querying GitHub for " + username +"'s repositories...</span>");
     
      var target = this;
      $.githubUser(username, function(data) {
        var repos = data.data; // JSON Parsing
        sortByName(repos);	
     
        var list = $('<dl/>');
        target.empty().append(list);
        $(repos).each(function() {
			if (this.name != (username.toLowerCase()+'.github.io')) {
				if ((this.description).indexOf(keyword) >= 0) {
				list.append('<dt><a href="'+ (this.homepage?this.homepage:this.html_url) +'">' + this.name + '</a> <em>'+(this.language?('('+this.language+')'):'')+'</em></dt>');
				list.append('<dd>' + this.description +'</dd>');
				list.append('<dd><em>Size: '+(this.size<1000?(this.size+' kB'):(Math.round((this.size/1000)*100)/100+' MB'))+' - ' +'Watchers: '+this.watchers+' - Forks: '+this.forks+' </em></dd>');
				list.append('<dd><br/></dd>');
		  }
			}
        });		
      });
	  
      function sortByName(repos) {
        repos.sort(function(a,b) {
          return a.name - b.name;
        });
      }
    };

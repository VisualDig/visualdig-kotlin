(function() {
    var elm = Elm.Main.fullscreen()

    // courtesy of github.com/kigiri
    var levenshteinDistance = function(a, b) {
                                if (a.length === 0) return b.length
                                if (b.length === 0) return a.length
                                let tmp, i, j, prev, val, row
                                // swap to save some memory O(min(a,b)) instead of O(a)
                                if (a.length > b.length) {
                                  tmp = a
                                  a = b
                                  b = tmp
                                }

                                row = Array(a.length + 1)
                                // init the row
                                for (i = 0; i <= a.length; i++) {
                                  row[i] = i
                                }

                                // fill in the rest
                                for (i = 1; i <= b.length; i++) {
                                  prev = i
                                  for (j = 1; j <= a.length; j++) {
                                    if (b[i - 1] === a[j - 1]) {
                                      val = row[j - 1] // match
                                    } else {
                                      val = Math.min(row[j - 1] + 1, // substitution
                                            Math.min(prev + 1,     // insertion
                                                     row[j] + 1))  // deletion
                                    }
                                    row[j - 1] = prev
                                    prev = val
                                  }
                                  row[a.length] = prev
                                }
                                return row[a.length]
                              }

    var findTextSearch = function(text){
                              var iframe = document.getElementById('siteUnderTest').contentDocument;
                              var n, found=false, a=[], walk=iframe.createTreeWalker(iframe.body,NodeFilter.SHOW_TEXT,null,false);
                              while(n=walk.nextNode()) {
                                if(n.textContent.search(text) != -1) {
                                    found = true
                                }
                                a.push(n.textContent);
                              }
                              return {
                                      allStrings: a,
                                      found: found
                                      };
                        };

    elm.ports.findText_search.subscribe(function(text) {
        var searchResult = findTextSearch(text);
        if(searchResult.found) {
            elm.ports.findText_searchResult.send({result: "Success",
                                             closestMatches: []})
        } else {
            var sortedCloseMatches = searchResult.allStrings.sort(function (first, second) {
                                                                                     return levenshteinDistance(text, first) - levenshteinDistance(text, second);
                                                                                  });
            elm.ports.findText_searchResult.send({result: "Failure",
                                             closestMatches: sortedCloseMatches});
        }
    });
})();
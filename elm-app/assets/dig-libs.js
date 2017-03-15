(function() {
    var elm = Elm.Main.fullscreen()

    window.dig = {}

    window.nextHoleId = 0

    // courtesy of github.com/kigiri
    window.dig.levenshteinDistance =
        function(a, b) {
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
          };

    window.dig.getTextFromNode =
        function(node) {
            if(node.outerText && node.outerText !== "") {
                return node.outerText;
            } else if (node.type === "button" && node.value !== "") {
                return node.value;
            }
            return null;
         };

    window.dig.findTextSearch =
        function(text){
            var iframe = document.getElementById('siteUnderTest').contentDocument;
            var n, textElement=null, a=[], walk=iframe.createTreeWalker(iframe.body,
                                                                 NodeFilter.SHOW_ELEMENT,
                                                                 null,
                                                                 false);
            while(n=walk.nextNode()) {
                var textFromNode = dig.getTextFromNode(n);
                if(textFromNode) {
                    if(textFromNode.search(text) != -1) {
                        textElement = n;
                    }
                    distance = dig.levenshteinDistance(text.toLowerCase(), textFromNode.toLowerCase());
                    a.push({term: textFromNode, distance: distance});
                }
            }

            return {
                  allStrings: a,
                  found: textElement
                  };
         };

     window.dig.clickPageNode =
        function(nodeList) {
            if(nodeList.length === 1) {
                nodeList[0].click();
            } else {
                console.log("Found a list of nodes for a specific digId ", nodeList)
            }
        };

    window.dig.elmFindTextSearch =
        function(text) {
            var searchResult = dig.findTextSearch(text);
            if(searchResult.found) {
                var holeId = window.nextHoleId;
                window.nextHoleId += 1;
                searchResult.found.dataset.digId = holeId;
                elm.ports.findText_searchResult.send({result: "Success",
                                                      digId: holeId,
                                                      closestMatches: []})
            } else {
                searchResult.allStrings.sort(function(a,b) {return a.distance - b.distance;});
                elm.ports.findText_searchResult.send({result: "Failure",
                                                      digId: null,
                                                      closestMatches: searchResult.allStrings.map(function(a) {return a.term})});
            }
        };

    window.dig.elmClickSearch =
        function(clickAction) {
            var iframe = document.getElementById('siteUnderTest').contentDocument;
            var nodeList = iframe.querySelectorAll('[data-dig-id="' + clickAction.digId + '"]');
            if(nodeList.length > 0 ) {
                dig.clickPageNode(nodeList);
                elm.ports.click_searchResult.send({result: "Success",
                                                   message: ""})
            } else {
                console.log('Couldnt find it element to click on! ', clickAction);r
                // execute another search using text query from before
                elm.ports.click_searchResult.send({result: "Failure", message: "Text element not visible yet"})
            }
        };


    // Hook into all elm events below
    elm.ports.findText_search.subscribe(dig.elmFindTextSearch);
    elm.ports.click_searchText.subscribe(dig.elmClickSearch);

})();
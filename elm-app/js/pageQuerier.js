export default function PageQuerier(document) {
    this.document = document;
}

PageQuerier.prototype.levenshteinDistance = function (a, b) {
    // courtesy of github.com/kigiri
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
                    Math.min(prev + 1, // insertion
                        row[j] + 1)) // deletion
            }
            row[j - 1] = prev
            prev = val
        }
        row[a.length] = prev
    }
    return row[a.length]
}

PageQuerier.prototype.getTextFromNode = function (node) {
    if (node.outerText && node.outerText !== "") {
        return node.outerText;
    } else if (node.type === "button" && node.value !== "") {
        return node.value;
    }
    return null;
}

PageQuerier.prototype.getElementByDigId = function (id) {
    var iframe = this.document.getElementById('siteUnderTest').contentDocument;
    var nodeList = iframe.querySelectorAll('[data-dig-id="' + id + '"]');
    if (nodeList.length > 0) {
        return nodeList[0];
    } else {
        return null;
    }
}

PageQuerier.prototype.findTextSearch = function (text) {
    var iframe = this.document.getElementById('siteUnderTest').contentDocument;
    var n, textElement = null,
        a = [],
        walk = iframe.createTreeWalker(iframe.body,
            NodeFilter.SHOW_ELEMENT,
            null,
            false);
    while (n = walk.nextNode()) {
        var textFromNode = this.getTextFromNode(n);
        if (textFromNode) {
            if (textFromNode.search(text) != -1) {
                textElement = n;
            }
            var distance = this.levenshteinDistance(text.toLowerCase(), textFromNode.toLowerCase());
            a.push({
                term: textFromNode,
                distance: distance
            });
        }
    }

    return {
        allStrings: a,
        found: textElement
    };
}

PageQuerier.prototype.clickPageNode = function (node) {
    node.click();
}

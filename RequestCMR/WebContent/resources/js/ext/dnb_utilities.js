/* A function to ensure the container is always centered.
*/

function setMargins() {
	    width = $(window).width();
	    containerWidth = $("#container").width();  
	    leftMargin = (width-containerWidth)/2;    
	    $("#container").css("marginLeft", leftMargin);    
	}


/* A function to extract json elements safely from an unpredictable JSON structure.
Parameters:
    json: the incoming json payload to parse (must be decoded into javascript object).
    selector:  the selector string in the form of 'PrimaryAddress.StreetAddressLine[0].LineText' or 'PrimaryAddress.StreetAddressLine.0.LineText'
    defaultVal: the default value to return if the element doesn't exist.  Blank by default.
    strictArrays:  If false, will assume element [0] when parsing the json and an index for an array wasn't specified.
        Additionally will remove indexes if provided for non-array elements in the json tree.  Default to false.
*/
function getJsonElement(json, selector, defaultVal, strictArrays){
    defaultVal = typeof defaultVal !== 'undefined' ? defaultVal : ''; // default
    strictArrays = typeof strictArrays !== 'undefined'? strictArrays : false; // default

    if(selector.length==0 || typeof json === 'undefined') { // base case
        return json ? json : defaultVal;
    }

    var cSelector; // current selector
    var newSelector;
    var pIndex = selector.indexOf('.');
    if(pIndex != -1){
        cSelector = selector.substring(0, pIndex);

        // Remove any bracketed indexes such as data[0] and tack them onto the next selector string.
        var arrayIndex = '';
        var bracketIndex = cSelector.indexOf('[');
        if(bracketIndex != -1){
            var rBracketIndex = cSelector.indexOf(']');
            if(rBracketIndex == -1)
                return defaultVal;

            arrayIndex = cSelector.substring(bracketIndex+1, rBracketIndex);
            cSelector = cSelector.substring(0, bracketIndex);
        }

        if(arrayIndex)
            newSelector = arrayIndex + '.' + selector.substring(pIndex+1, selector.length); // remaining
        else
            newSelector = selector.substring(pIndex+1, selector.length);

    } else {
        cSelector = selector;
        newSelector = '';
    }

    // fix arrays if not in strict mode
    if(!strictArrays){
        // If the json is an array and the selector isn't an index, select index 0, push current selector to next iteration.
        if((json instanceof Array) && isNaN(cSelector)){
            newSelector = selector;
            cSelector = 0;
        }
        // If the json isn't an array and the selector is an index, try string literal and drop index if nonexistent.
        else if (!(json instanceof Array) && !isNaN(cSelector)){
            // Try selection by field literal (fields names can be numbers)
            if(typeof json[cSelector] === 'undefined'){
                // If invalid, discard index and move into next selector.
                return getJsonElement(json, newSelector, defaultVal, strictArrays)
            }

        }
    }

    try{
        return getJsonElement(json[cSelector], newSelector, defaultVal, strictArrays);
    } catch(err){
        return defaultVal;
    }
}

/* A function to flatten a javascript object, likely constructed from json returned by Direct services.
Will return an array of elements containing a name, a full name, a friendly name, and indentation level, and a value.
 */
function flatten(obj)
{
    var _transformName = function(arr)
    {
        var nm = arr[arr.length-1];
        if (nm=='$')
            nm = arr[arr.length-2];
        if (nm[0]=='@')
            nm = arr[arr.length-2] + ' ' + nm.substr(1);
        var nm2 = '';
        for (var i=0; i<nm.length; i++)
        {
            if (i>0 && nm[i].toUpperCase()==nm[i])
                nm2 += ' ';
            nm2 += nm[i];
        }
        nm2 = nm2.replace(/D N B/g, 'D&B');
        return nm2;
    }
    var _flatten = function(o, prefix)
    {
        if (typeof(prefix)=='undefined')
        {
            prefix = [];
        }
        var arr = [];
        for (var k in o)
        {
            if (!o.hasOwnProperty(k))
                continue;
            var nameParts = prefix.concat(k);
            if (typeof(o[k]) == 'object') {
                arr.push({
                    name: k,
                    fullName: nameParts,
                    friendlyName: _transformName(nameParts),
                    indent: nameParts.length-1,
                    value: null
                });
//                var addl = _flatten(o[k], nameParts);
                arr = arr.concat(_flatten(o[k], nameParts));
            }
            else
                arr.push({
                    name: k,
                    fullName: nameParts,
                    friendlyName: _transformName(nameParts),
                    indent: nameParts.length-1,
                    value: o[k]});
        }
        return arr;
    }

    var arr = _flatten(obj);
//    arr.sort(function(a,b) { return a.friendlyName==b.friendlyName?0:a.friendlyName<b.friendlyName?-1:1 })
    return arr;
}

/* A function to normalize scores into a float of range [0, 1].
 */
function normalizeScore(score, min, max, reversed)
{
    if(score == '-')
        return 1;

    if(score < min) score = min; //fix for customer file with bad scores.
    if(score > max) score = max; //fix for customer file with bad scores.

    if (!isNaN(score)){ //Number
        // score is 1-9
        // 1->100%
        // 9->0%
        if(reversed)
            score = max - score;
    } else { //String CUSTOM solution for VIAB Rating
        // A-G and H-M
        if (score>='A' && score<='G') {
            score = score.charCodeAt(0)
            min = 'A'.charCodeAt(0); // -> 100
            max = 'G'.charCodeAt(0); // -> 0
            score = max - score;
        } else if (score<='Z') {
            score = score.charCodeAt(0)
            min = 'A'.charCodeAt(0); // -> 100
            max = 'Z'.charCodeAt(0); // -> 0
            score = max - score;
        }
    }
    var range = max - min;
    score = score / range;
    return score;
}

/* A function to convert a normalized score into a color value
 */
function colorValToColor(num) {
    var colors = ['#DA736D', '#f7b41f', '#30a567'];
    var colorNum = num * (colors.length-1);
    var diffBetweenColors = colorNum - Math.floor(colorNum);
    var num1 = Math.floor(colorNum);
    var num2 = Math.ceil(colorNum);
    var color1 = colors[num1];
    var color2 = colors[num2];
    var color = blendColor(color1, color2, diffBetweenColors);
    return color;
}

/* A function to mathematically blend 2 colors based upon the blend value, where 0 <= blend <= 1
 */
function blendColor(hexColor1, hexColor2, blend)
{
    if (typeof(blend)==='undefined')
        blend = 0.5;
    var c1 = hexToRgb(hexColor1);
    var c2 = hexToRgb(hexColor2);
    var color = [];
    for(var i=0; i<3; i++)
    {
        var delta = c2[i] - c1[i];
        var c = c1[i] + blend * delta;
        color[i] = c;
    }
    return rgbToHex(color);
}

/* Helper function to convert RGB colors to hex values
 */
function rgbToHex(rgb) {
    var componentToHex = function(c) {
        var hex = Math.round(c).toString(16);

        return hex.length == 1 ? "0" + hex : hex;
    }
    return "#" + componentToHex(rgb[0]) + componentToHex(rgb[1]) + componentToHex(rgb[2]);
}

/* Helper function to convert hex values to RGB color values
 */
function hexToRgb(hex) {
    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? [
        parseInt(result[1], 16),
        parseInt(result[2], 16),
        parseInt(result[3], 16)
    ] : null;
}

function isEmpty(str) {
    return (!str || 0 === str.length);
}
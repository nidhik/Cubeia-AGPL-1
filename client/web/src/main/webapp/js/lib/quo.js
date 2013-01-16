/*
    QuoJS 2.1
    http://quojs.tapquo.com

    Copyright (C) 2011,2012 Javi Jiménez Villar (@soyjavi)

    Permission is hereby granted, free of charge, to any person obtaining a
    copy of this software and associated documentation files (the "Software"),
    to deal in the Software without restriction, including without limitation
    the rights to use, copy, modify, merge, publish, distribute, sublicense,
    and/or sell copies of the Software, and to permit persons to whom the
    Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
    FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
    DEALINGS IN THE SOFTWARE.
*/

(function(){var a;a=function(){var j,i,b;i=[];b=function(f,n){f=f||i;f.__proto__=b.prototype;f.selector=n||"";return f};j=function(f,n){var p;if(f){p=j.getDOMObject(f,n);if(n)f+=" "+n;return b(p,f)}else return b()};j.extend=function(f){Array.prototype.slice.call(arguments,1).forEach(function(n){var p,s;s=[];for(p in n)s.push(f[p]=n[p]);return s});return f};b.prototype=j.fn={};return j}();window.Quo=a;"$$"in window||(window.$$=a)}).call(this);(function(){(function(a){var j,i,b,f,n,p,s,r;j=[];f=Object.prototype;b=/^\s*<(\w+|!)[^>]*>/;n=document.createElement("table");p=document.createElement("tr");i={tr:document.createElement("tbody"),tbody:n,thead:n,tfoot:n,td:p,th:p,"*":document.createElement("div")};a.toType=function(h){return f.toString.call(h).match(/\s([a-z|A-Z]+)/)[1].toLowerCase()};a.isOwnProperty=function(h,m){return f.hasOwnProperty.call(h,m)};a.getDOMObject=function(h,m){var k,d,c;k=null;d=[1,9,11];c=a.toType(h);if(c==="array")k=
s(h);else if(c==="string"&&b.test(h)){k=a.fragment(h.trim(),RegExp.$1);h=null}else if(c==="string"){k=a.query(document,h);if(m)k=k.length===1?a.query(k[0],m):a.map(function(){return a.query(k,m)})}else if(d.indexOf(h.nodeType)>=0||h===window){k=[h];h=null}return k};a.map=function(h,m){var k,d,c;c=[];k=void 0;if(a.toType(h)==="array")for(k=0;k<h.length;){d=m(h[k],k);d!=null&&c.push(d);k++}else for(k in h){d=m(h[k],k);d!=null&&c.push(d)}return r(c)};a.each=function(h,m){var k;k=void 0;if(a.toType(h)===
"array")for(k=0;k<h.length;){if(m.call(h[k],k,h[k])===false)break;k++}else for(k in h)if(m.call(h[k],k,h[k])===false)break;return h};a.mix=function(){var h,m,k,d,c;k={};h=0;for(d=arguments.length;h<d;){m=arguments[h];for(c in m)if(a.isOwnProperty(m,c)&&m[c]!==undefined)k[c]=m[c];h++}return k};a.fragment=function(h,m){var k;if(m==null)m="*";m in i||(m="*");k=i[m];k.innerHTML=""+h;return a.each(Array.prototype.slice.call(k.childNodes),function(){return k.removeChild(this)})};a.fn.map=function(h){return a.map(this,
function(m,k){return h.call(m,k,m)})};a.fn.instance=function(h){return this.map(function(){return this[h]})};a.fn.filter=function(h){return a([].filter.call(this,function(m){return m.parentNode&&a.query(m.parentNode,h).indexOf(m)>=0}))};a.fn.forEach=j.forEach;a.fn.indexOf=j.indexOf;s=function(h){return h.filter(function(m){return m!==void 0&&m!==null})};r=function(h){return h.length>0?[].concat.apply([],h):h}})(Quo)}).call(this);(function(){(function(a){a.fn.attr=function(j,i){return a.toType(j)==="string"&&i===void 0?this[0].getAttribute(j):this.each(function(){return this.setAttribute(j,i)})};a.fn.data=function(j,i){return this.attr("data-"+j,i)};a.fn.val=function(j){return a.toType(j)==="string"?this.each(function(){return this.value=j}):this.length>0?this[0].value:null};a.fn.show=function(){return this.style("display","block")};a.fn.hide=function(){return this.style("display","none")};a.fn.height=function(){return this.offset().height};
a.fn.width=function(){return this.offset().width};a.fn.offset=function(){var j;j=this[0].getBoundingClientRect();return{left:j.left+window.pageXOffset,top:j.top+window.pageYOffset,width:j.width,height:j.height}};a.fn.remove=function(){return this.each(function(){if(this.parentNode!=null)return this.parentNode.removeChild(this)})}})(Quo)}).call(this);(function(){(function(a){var j,i,b,f,n,p,s;b=null;j=/WebKit\/([\d.]+)/;i={Android:/(Android)\s+([\d.]+)/,ipad:/(iPad).*OS\s([\d_]+)/,iphone:/(iPhone\sOS)\s([\d_]+)/,blackberry:/(BlackBerry).*Version\/([\d.]+)/,webos:/(webOS|hpwOS)[\s\/]([\d.]+)/};a.isMobile=function(){b=b||n();return b.isMobile};a.environment=function(){return b=b||n()};a.isOnline=function(){return navigator.onLine};n=function(){var r,h;h=navigator.userAgent;r={};r.browser=f(h);r.os=p(h);r.isMobile=r.os?true:false;r.screen=s();return r};
f=function(r){var h;return(h=r.match(j))?h[0]:r};p=function(r){var h,m,k;h=void 0;for(m in i)if(k=r.match(i[m])){h={name:m==="iphone"||m==="ipad"?"ios":m,version:k[2].replace("_",".")};break}return h};s=function(){return{width:window.innerWidth,height:window.innerHeight}}})(Quo)}).call(this);(function(){(function(a){var j;a.fn.text=function(i){return i?this.each(function(){return this.textContent=i}):this[0].textContent};a.fn.html=function(i){var b;b=a.toType(i);return i?this.each(function(){if(b==="string"||b==="number")return this.innerHTML=i;else{this.innerHTML=null;return this.appendChild(i)}}):this[0].innerHTML};a.fn.append=function(i){return this.each(function(){if(a.toType(i)==="string"){if(i)return this.appendChild(j(i))}else return this.insertBefore(i)})};a.fn.prepend=function(i){return this.each(function(){var b;
if(a.toType(i)==="string")return this.innerHTML=i+this.innerHTML;else{b=this.parentNode;return b.insertBefore(i,b.firstChild)}})};a.fn.replaceWith=function(i){return this.each(function(){var b;if(a.toType(i)==="string")i=j(i);(b=this.parentNode)&&b.insertBefore(i,this);return a(this).remove()})};a.fn.empty=function(){return this.each(function(){this.innerHTML=null})};j=function(i){var b;b=document.createElement("div");b.innerHTML=i;return b.firstChild}})(Quo)}).call(this);(function(){(function(a){var j,i;a.query=function(b,f){var n;n=b.querySelectorAll(f);return n=Array.prototype.slice.call(n)};a.fn.find=function(b){var f;f=this.length===1?Quo.query(this[0],b):this.map(function(){return Quo.query(this,b)});return a(f)};a.fn.parent=function(b){var f;f=b?i(this):this.instance("parentNode");return j(f,b)};a.fn.siblings=function(b){var f;f=this.map(function(n,p){return Array.prototype.slice.call(p.parentNode.children).filter(function(s){return s!==p})});return j(f,b)};
a.fn.children=function(b){var f;f=this.map(function(){return Array.prototype.slice.call(this.children)});return j(f,b)};a.fn.get=function(b){return b===undefined?this:this[b]};a.fn.first=function(){return a(this[0])};a.fn.last=function(){return a(this[this.length-1])};a.fn.closest=function(b,f){var n,p;p=this[0];n=a(b);for(n.length||(p=null);p&&n.indexOf(p)<0;)p=p!==f&&p!==document&&p.parentNode;return a(p)};a.fn.each=function(b){this.forEach(function(f,n){return b.call(f,n,f)});return this};i=function(b){var f;
for(f=[];b.length>0;)b=a.map(b,function(n){if((n=n.parentNode)&&n!==document&&f.indexOf(n)<0){f.push(n);return n}});return f};j=function(b,f){return f===undefined?a(b):a(b).filter(f)}})(Quo)}).call(this);(function(){(function(a){var j,i;a.fn.addClass=function(b){return this.each(function(){if(!i(b,this.className)){this.className+=" "+b;return this.className=this.className.trim()}})};a.fn.removeClass=function(b){return this.each(function(){if(b){if(i(b,this.className))return this.className=this.className.replace(b," ").replace(/\s+/g," ").trim()}else return this.className=""})};a.fn.toggleClass=function(b){return this.each(function(){if(i(b,this.className))return this.className=this.className.replace(b,
" ");else{this.className+=" "+b;return this.className=this.className.trim()}})};a.fn.hasClass=function(b){return i(b,this[0].className)};a.fn.style=function(b,f){return f?this.each(function(){return this.style[b]=f}):this[0].style[b]||j(this[0],b)};i=function(b,f){return f.split(/\s+/g).indexOf(b)>=0};j=function(b,f){return document.defaultView.getComputedStyle(b,"")[f]}})(Quo)}).call(this);(function(){(function(a){var j,i,b,f,n,p,s,r,h,m,k;j={TYPE:"GET",MIME:"json"};b={script:"text/javascript, application/javascript",json:"application/json",xml:"application/xml, text/xml",html:"text/html",text:"text/plain"};i=0;a.ajaxSettings={type:j.TYPE,async:true,success:{},error:{},context:null,dataType:j.MIME,headers:{},xhr:function(){return new window.XMLHttpRequest},crossDomain:false,timeout:0};a.ajax=function(d){var c,e,l;e=a.mix(a.ajaxSettings,d);if(e.type===j.TYPE)e.url+=a.serializeParameters(e.data,
"?");else e.data=a.serializeParameters(e.data);if(f(e.url))return a.jsonp(e);l=e.xhr();l.onreadystatechange=function(){if(l.readyState===4){clearTimeout(c);return h(l,e)}};l.open(e.type,e.url,e.async);r(l,e);if(e.timeout>0)c=setTimeout(function(){return k(l,e)},e.timeout);l.send(e.data);return e.async?l:n(l,e)};a.jsonp=function(d){var c,e,l,q;if(d.async){e="jsonp"+ ++i;l=document.createElement("script");q={abort:function(){a(l).remove();if(e in window)return window[e]={}}};c=void 0;window[e]=function(u){clearTimeout(c);
a(l).remove();delete window[e];return m(u,q,d)};l.src=d.url.replace(/=\?/,"="+e);a("head").append(l);if(d.timeout>0)c=setTimeout(function(){return k(q,d)},d.timeout);return q}else return console.error("QuoJS.ajax: Unable to make jsonp synchronous call.")};a.get=function(d,c,e,l){return a.ajax({url:d,data:c,success:e,dataType:l})};a.post=function(d,c,e,l){return s("POST",d,c,e,l)};a.put=function(d,c,e,l){return s("PUT",d,c,e,l)};a["delete"]=function(d,c,e,l){return s("DELETE",d,c,e,l)};a.json=function(d,
c,e){return a.ajax({url:d,data:c,success:e,dataType:j.MIME})};a.serializeParameters=function(d,c){var e,l;if(c==null)c="";l=c;for(e in d)if(d.hasOwnProperty(e)){if(l!==c)l+="&";l+=e+"="+d[e]}return l===c?"":l};h=function(d,c){if(d.status===200||d.status===0)c.async&&m(n(d,c),d,c);else p("QuoJS.ajax: Unsuccesful request",d,c)};m=function(d,c,e){e.success.call(e.context,d,c)};p=function(d,c,e){e.error.call(e.context,d,c,e)};r=function(d,c){var e;if(c.contentType)c.headers["Content-Type"]=c.contentType;
if(c.dataType)c.headers.Accept=b[c.dataType];for(e in c.headers)d.setRequestHeader(e,c.headers[e])};k=function(d,c){d.onreadystatechange={};d.abort();p("QuoJS.ajax: Timeout exceeded",d,c)};s=function(d,c,e,l,q){return a.ajax({type:d,url:c,data:e,success:l,dataType:q,contentType:"application/x-www-form-urlencoded"})};n=function(d,c){var e;if(e=d.responseText)if(c.dataType===j.MIME)try{e=JSON.parse(e)}catch(l){e=l;p("QuoJS.ajax: Parse Error",d,c)}else if(c.dataType==="xml")e=d.responseXML;return e};
f=function(d){return/=\?/.test(d)}})(Quo)}).call(this);(function(){(function(a){var j,i;j=/complete|loaded|interactive/;i={touch:"touchstart",tap:"tap"};["touch","tap"].forEach(function(b){a.fn[b]=function(f){return a(document.body).delegate(this.selector,i[b],f)};return this});a.fn.on=function(b,f,n){return f===undefined||a.toType(f)==="function"?this.bind(b,f):this.delegate(f,b,n)};a.fn.off=function(b,f,n){return f===undefined||a.toType(f)==="function"?this.unbind(b,f):this.undelegate(f,b,n)};a.fn.ready=function(b){j.test(document.readyState)?b(a):
a.fn.addEvent(document,"DOMContentLoaded",function(){return b(a)});return this}})(Quo)}).call(this);(function(){(function(a){var j,i,b,f,n,p,s,r,h,m,k;j=1;f={};b={preventDefault:"isDefaultPrevented",stopImmediatePropagation:"isImmediatePropagationStopped",stopPropagation:"isPropagationStopped"};i={touchstart:"mousedown",touchmove:"mousemove",touchend:"mouseup",tap:"click",doubletap:"dblclick",orientationchange:"resize"};a.Event=function(d,c){var e;e=document.createEvent("Events");e.initEvent(d,true,true,null,null,null,null,null,null,null,null,null,null,null,null);if(c){e.pageX=c.x1;e.pageY=c.y1;
e.toX=c.x2;e.toY=c.y2;e.fingers=c.fingers}return e};a.fn.bind=function(d,c){return this.each(function(){m(this,d,c)})};a.fn.unbind=function(d,c){return this.each(function(){k(this,d,c)})};a.fn.delegate=function(d,c,e){return this.each(function(l,q){m(q,c,e,d,function(u){return function(x){var y,v;if(v=a(x.target).closest(d,q).get(0)){y=a.extend(n(x),{currentTarget:v,liveFired:q});return u.apply(v,[y].concat([].slice.call(arguments,1)))}}})})};a.fn.undelegate=function(d,c,e){return this.each(function(){k(this,
c,e,d)})};a.fn.trigger=function(d,c){if(a.toType(d)==="string")d=a.Event(d,c);return this.each(function(){this.dispatchEvent(d)})};a.fn.addEvent=function(d,c,e){return d.addEventListener?d.addEventListener(c,e,false):d.attachEvent?d.attachEvent("on"+c,e):d["on"+c]=e};a.fn.removeEvent=function(d,c,e){return d.removeEventListener?d.removeEventListener(c,e,false):d.detachEvent?d.detachEvent("on"+c,e):d["on"+c]=null};m=function(d,c,e,l,q){var u;c=s(c);u=h(d);u=f[u]||(f[u]=[]);q=q&&q(e,c);c={event:c,callback:e,
selector:l,proxy:p(q,e,d),delegate:q,index:u.length};u.push(c);return a.fn.addEvent(d,c.event,c.proxy)};k=function(d,c,e,l){var q;c=s(c);q=h(d);return r(q,c,e,l).forEach(function(u){delete f[q][u.index];return a.fn.removeEvent(d,u.event,u.proxy)})};h=function(d){return d._id||(d._id=j++)};s=function(d){return(a.isMobile()?d:i[d])||d};p=function(d,c,e){c=d||c;return function(l){var q;q=c.apply(e,[l].concat(l.data));q===false&&l.preventDefault();return q}};r=function(d,c,e,l){return(f[d]||[]).filter(function(q){return q&&
(!c||q.event===c)&&(!e||q.fn===e)&&(!l||q.selector===l)})};n=function(d){var c;c=a.extend({originalEvent:d},d);a.each(b,function(e,l){c[e]=function(){this[l]=function(){return true};return d[e].apply(d,arguments)};return c[l]=function(){return false}});return c}})(Quo)}).call(this);(function(){(function(a){var j,i,b,f,n,p,s,r,h,m,k,d,c,e,l,q,u,x,y,v;b={};i=[];j=[];f=void 0;["doubleTap","hold","swipe","swiping","swipeLeft","swipeRight","swipeUp","swipeDown","rotate","rotating","rotateLeft","rotateRight","pinch","pinching","pinchIn","pinchOut","drag"].forEach(function(g){a.fn[g]=function(o){return this.on(g,o)}});a(document).ready(function(){return e()});e=function(){var g;g=a(document.body);g.bind("touchstart",u);g.bind("touchmove",q);g.bind("touchend",l);return g.bind("touchcancel",
r)};u=function(g){var o,t,w;t=Date.now();o=t-(b.last||t);f&&clearTimeout(f);w=k(g);g=w.length;i=m(w,g);b.el=a(x(w[0].target));b.fingers=g;b.last=t;if(g===1){b.isDoubleTap=o>0&&o<=250;return setTimeout(d,650)}else if(g===2){b.initial_angle=parseInt(n(i),10);b.initial_distance=parseInt(h(i),10);b.angle_difference=0;return b.distance_difference=0}};q=function(g){var o,t;if(b.el){t=k(g);o=t.length;if(o===b.fingers){j=m(t,o);c(g)&&v("swiping");if(o===2){s();p()}}else r()}return true};c=function(){var g,
o;g=false;if(j[0]){g=Math.abs(i[0].x-j[0].x)>30;o=Math.abs(i[0].y-j[0].y)>30;g=b.el&&(g||o)}return g};l=function(){var g;if(b.isDoubleTap){v("doubleTap");return r()}else if(b.fingers===1)if(c()){v("swipe");g=y(i[0].x,j[0].x,i[0].y,j[0].y);v(g);return r()}else{v("tap");return f=setTimeout(r,250)}else if(b.fingers===2){if(b.angle_difference!==0){v("rotate",{angle:b.angle_difference});g=b.angle_difference>0?"rotateRight":"rotateLeft";v(g,{angle:b.angle_difference})}if(b.distance_difference!==0){v("pinch",
{angle:b.distance_difference});g=b.distance_difference>0?"pinchOut":"pinchIn";v(g,{distance:b.distance_difference})}return r()}};m=function(g,o){var t,w;w=[];for(t=0;t<o;){w.push({x:g[t].pageX,y:g[t].pageY});t++}return w};s=function(){var g,o,t;g=parseInt(n(j),10);g=parseInt(b.initial_angle-g,10);if(Math.abs(g)>10||b.angle_difference!==0){o=0;for(t=b.angle_difference<0?"-":"+";Math.abs(g-b.angle_difference)>90&&o++<10;)eval("diff "+t+"= 180;");b.angle_difference=parseInt(g,10);return v("rotating",
{angle:b.angle_difference})}};p=function(){var g;g=parseInt(h(j),10);g=b.initial_distance-g;if(Math.abs(g)>5){b.distance_difference=g;return v("pinching",{distance:g})}};v=function(g,o){if(b.el){o=o||{};return b.el.trigger(g,o)}};r=function(){i=[];j=[];b={};return clearTimeout(f)};n=function(g){var o;o=g[0];g=g[1];o=Math.atan((g.y-o.y)*-1/(g.x-o.x))*(180/Math.PI);return o<0?o+180:o};h=function(g){var o;o=g[0];g=g[1];return Math.sqrt((g.x-o.x)*(g.x-o.x)+(g.y-o.y)*(g.y-o.y))*-1};k=function(g){return a.isMobile()?
g.touches:[g]};x=function(g){return"tagName"in g?g:g.parentNode};y=function(g,o,t,w){return Math.abs(g-o)>=Math.abs(t-w)?g-o>0?"swipeLeft":"swipeRight":t-w>0?"swipeUp":"swipeDown"};d=function(){if(b.last&&Date.now()-b.last>=650){v("hold");return r()}}})(Quo)}).call(this);

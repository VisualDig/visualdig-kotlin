import css from './css/main.css';
import ElmApp from './elmApp';
import {
    PortBindings
} from './portBindings';

import PageQuerier from './pageQuerier';

var app = new ElmApp();
var pageQuerier = new PageQuerier(document);
var portBindings = new PortBindings(app.elm, pageQuerier);

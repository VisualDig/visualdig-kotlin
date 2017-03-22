import css from './css/main.css';
import ElmApp from './elmApp';
import PortBindings from './portBindings';
import PageQuerier from './pageQuerier';
import SearchService from './searchService';
import Geometry from './geometry';

var app = new ElmApp();
var geometry = new Geometry();
var pageQuerier = new PageQuerier(document, geometry);
var searchService = new SearchService(pageQuerier);
var portBindings = new PortBindings(app.elm, pageQuerier, searchService);

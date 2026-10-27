(function () {
  'use strict';

  // rooms are 90px squares placed 180px apart, dragged rooms snap to that grid
  var ROOM_GRID = 180;
  var ZOOM_STEP = 1.1;
  var MIN_SCALE = 0.05;
  var MAX_SCALE = 5;
  var DRAG_THRESHOLD = 4;
  var NOTES_SAVE_DELAY = 400;
  var COMPASS_DIRS = ['n', 'ne', 'e', 'se', 's', 'sw', 'w', 'nw'];
  // exits whose names are obvious from the direction of the line, their labels only show on hover
  var COMPASS_EXIT_NAMES = new Set(['n', 'north', 'ne', 'northeast', 'e', 'east', 'se', 'southeast', 's', 'south',
    'sw', 'southwest', 'w', 'west', 'nw', 'northwest', 'u', 'up', 'd', 'down']);
  // softer versions of the room highlight colors the mapper saves
  var PRETTY_COLORS = {
    '#ff0000': '#d9534f',
    '#ffff00': '#e6c84a',
    '#0000ff': '#3f6fd9',
    '#ff00ff': '#cf5fb8',
    '#a020f0': '#8b5cf6',
    '#ff9100': '#ef8a35',
    '#8b4513': '#8a5a36',
    '#40e0d0': '#3cbfae',
    '#fffff0': '#ece6d6'
  };
  var NAMES_MIN_SCALE = 0.75;
  var GRID_MIN_SIZE = 24;
  // exits going both ways between two rooms are drawn as two lanes this far apart from the middle
  var EDGE_LANE_OFFSET = 4;

  var statusEl = document.getElementById('status');
  var statusTextEl = statusEl.querySelector('.status-text');
  var areaNameEl = document.getElementById('area-name');
  var views = {};
  var activeView = null;
  var statusTimer = null;

  function storageGet(key) {
    try { return window.localStorage.getItem(key); } catch (e) { return null; }
  }

  function storageSet(key, value) {
    try { window.localStorage.setItem(key, value); } catch (e) { /* not available, fine */ }
  }

  function api(view, action, data) {
    var body = Object.assign({ view: view, action: action }, data || {});
    return fetch('api', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-Batmap': '1' },
      body: JSON.stringify(body)
    }).then(function (response) {
      if (!response.ok) {
        return response.text().then(function (text) { throw new Error(text || response.statusText); });
      }
      return response.json();
    });
  }

  function showError(error) {
    console.error(error);
    statusTextEl.textContent = 'error: ' + error.message;
    statusEl.classList.remove('connected');
    statusEl.classList.add('error');
    clearTimeout(statusTimer);
    statusTimer = setTimeout(updateStatus, 4000);
  }

  function updateStatus() {
    var connected = Object.keys(views).every(function (name) { return views[name].connected; });
    statusTextEl.textContent = connected ? 'connected' : 'disconnected, is batclient running?';
    statusEl.classList.toggle('connected', connected);
    statusEl.classList.remove('error');
  }

  function prettyColor(color) {
    return color ? PRETTY_COLORS[color.toLowerCase()] || color : null;
  }

  function isLightColor(color) {
    var match = /^#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$/i.exec(color || '');
    if (!match) return false;
    var channels = match.slice(1).map(function (hex) {
      var c = parseInt(hex, 16) / 255;
      return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    });
    return 0.2126 * channels[0] + 0.7152 * channels[1] + 0.0722 * channels[2] > 0.4;
  }

  function el(tag, className, text) {
    var element = document.createElement(tag);
    if (className) element.className = className;
    if (text !== undefined && text !== null) element.textContent = text;
    return element;
  }

  // ---- dialogs ----

  var modal = {
    backdrop: document.getElementById('modal'),
    title: document.getElementById('modal-title'),
    body: document.getElementById('modal-body'),
    input: document.getElementById('modal-input'),
    buttons: document.getElementById('modal-buttons'),
    finish: null,

    // buttons: [{label, value, primary}], resolves with {value, input}, value is null when cancelled
    open: function (options) {
      var self = this;
      if (self.finish) self.finish(null);
      self.title.textContent = options.title || '';
      self.body.textContent = options.body || '';
      self.body.hidden = !options.body;
      self.input.hidden = options.input === undefined;
      self.input.value = options.input || '';
      self.buttons.replaceChildren();
      var primary = null;
      return new Promise(function (resolve) {
        self.finish = function (value) {
          self.finish = null;
          self.backdrop.hidden = true;
          resolve({ value: value, input: self.input.value });
        };
        options.buttons.forEach(function (button) {
          var b = el('button', button.primary ? 'primary' : '', button.label);
          b.type = 'button';
          b.addEventListener('click', function () { self.finish(button.value); });
          self.buttons.append(b);
          if (button.primary) primary = { element: b, value: button.value };
        });
        self.primary = primary;
        self.backdrop.hidden = false;
        if (!self.input.hidden) {
          self.input.focus();
          self.input.select();
        } else if (primary) {
          primary.element.focus();
        }
      });
    },

    confirm: function (title, body) {
      return this.open({
        title: title,
        body: body,
        buttons: [{ label: 'Cancel', value: false }, { label: 'Yes', value: true, primary: true }]
      }).then(function (result) { return result.value === true; });
    }
  };

  modal.backdrop.addEventListener('keydown', function (e) {
    if (!modal.finish) return;
    if (e.key === 'Escape') {
      e.preventDefault();
      modal.finish(null);
    } else if (e.key === 'Enter' && e.target === modal.input && modal.primary) {
      e.preventDefault();
      modal.finish(modal.primary.value);
    }
  });
  modal.backdrop.addEventListener('pointerdown', function (e) {
    if (e.target === modal.backdrop && modal.finish) modal.finish(null);
  });

  var help = document.getElementById('help');
  document.getElementById('help-button').addEventListener('click', function () {
    help.hidden = false;
    document.getElementById('help-close').focus();
  });
  document.getElementById('help-close').addEventListener('click', function () { help.hidden = true; });
  help.addEventListener('keydown', function (e) { if (e.key === 'Escape') help.hidden = true; });
  help.addEventListener('pointerdown', function (e) { if (e.target === help) help.hidden = true; });

  // ---- one map view, either the live map or the search map ----

  function MapView(name) {
    var template = document.getElementById('view-template');
    this.name = name;
    this.root = template.content.firstElementChild.cloneNode(true);
    this.root.dataset.view = name;
    document.getElementById('views').append(this.root);

    var q = this.root.querySelector.bind(this.root);
    this.viewport = q('.viewport');
    this.world = q('.world');
    this.edgeLayer = q('.edges');
    this.labelLayer = q('.exit-labels');
    this.roomLayer = q('.rooms');
    this.selectRect = q('.select-rect');
    this.emptyNote = q('.empty-note');
    this.roomCard = q('.room-card');
    this.shortDesc = q('.short-desc');
    this.longDesc = q('.long-desc');
    this.roomExits = q('.room-exits');
    this.notes = q('.notes');
    this.swatches = q('.swatches');
    this.saveButton = q('.save');
    this.clearButton = q('.clear');
    this.toggles = { snap: q('.snap'), maze: q('.maze'), revs: q('.revs') };
    this.searchbar = q('.searchbar');
    this.searchText = q('.search-text');
    this.searchResults = q('.search-results');
    this.areaList = q('.area-list');

    this.state = null;
    this.roomById = new Map();
    this.roomEls = new Map();
    this.edgeEls = new Map();
    this.labelEls = new Map();
    this.pickedExit = null;
    this.hoverExit = null;
    this.drag = null;
    this.pendingState = null;
    this.positionOverrides = null;
    this.overrideUntilVersion = Infinity;
    this.lastArea = undefined;
    this.lastCurrentKey = null;
    this.notesRoom = null;
    this.notesTimer = null;
    this.colorsKey = null;
    this.resultsKey = null;
    this.areasKey = null;
    this.connected = false;

    this.scale = parseFloat(storageGet('batmap.scale.' + name)) || 1;
    this.tx = 0;
    this.ty = 0;
    if (storageGet('batmap.side.' + name) === 'hidden') this.root.classList.add('no-side');

    this.searchbar.hidden = name !== 'search';
    this.bindEvents();
    this.applyTransform();
    this.connect();
  }

  MapView.prototype.act = function (action, data) {
    return api(this.name, action, data).catch(function (error) {
      showError(error);
      throw error;
    });
  };

  MapView.prototype.connect = function () {
    var self = this;
    var source = new EventSource('events?view=' + encodeURIComponent(this.name));
    source.addEventListener('state', function (e) {
      self.connected = true;
      updateStatus();
      self.receive(JSON.parse(e.data));
    });
    source.onopen = function () {
      self.connected = true;
      updateStatus();
    };
    source.onerror = function () {
      self.connected = false;
      updateStatus();
    };
  };

  MapView.prototype.receive = function (state) {
    // don't redraw under the user's mouse while rooms are being dragged
    if (this.drag) {
      this.pendingState = state;
      return;
    }
    this.apply(state);
  };

  MapView.prototype.apply = function (state) {
    var self = this;
    if (this.positionOverrides && state.version >= this.overrideUntilVersion) {
      this.positionOverrides = null;
    }
    this.state = state;
    this.roomById = new Map();
    state.rooms.forEach(function (room) {
      var override = self.positionOverrides && self.positionOverrides.get(room.id);
      if (override) {
        room.x = override.x;
        room.y = override.y;
      }
      self.roomById.set(room.id, room);
    });
    this.renderRooms();
    this.renderEdges();
    this.renderSide();
    this.renderSearch();
    this.renderEmptyNote();
    if (activeView === this) updateAreaName();
    this.follow();
  };

  // ---- drawing ----

  MapView.prototype.applyTransform = function () {
    this.world.style.transform = 'translate(' + this.tx + 'px,' + this.ty + 'px) scale(' + this.scale + ')';
    // one grid dot in the gap between every four room cells, rooms sit on multiples of the grid
    var size = ROOM_GRID * this.scale;
    var vs = this.viewport.style;
    vs.backgroundImage = size < GRID_MIN_SIZE ? 'none' : '';
    vs.backgroundSize = size + 'px ' + size + 'px';
    vs.backgroundPosition = this.tx + 'px ' + this.ty + 'px';
    this.world.classList.toggle('show-names', this.scale >= NAMES_MIN_SCALE);
  };

  MapView.prototype.renderRooms = function () {
    var self = this;
    var seen = new Set();
    this.state.rooms.forEach(function (room) {
      seen.add(room.id);
      var element = self.roomEls.get(room.id);
      if (!element) {
        element = el('div', 'room');
        element.dataset.id = room.id;
        self.roomLayer.append(element);
        self.roomEls.set(room.id, element);
      }
      var fill = room.explored ? null : prettyColor(room.color);
      var classes = 'room';
      if (room.indoors) classes += ' indoor';
      if (room.picked) classes += ' picked';
      if (room.current) classes += ' current';
      if (fill && isLightColor(fill)) classes += ' on-light';
      element.className = classes;
      element.style.left = room.x + 'px';
      element.style.top = room.y + 'px';
      if (room.explored) {
        element.style.setProperty('--fill', 'var(--room-explored)');
      } else if (fill) {
        element.style.setProperty('--fill', fill);
      } else {
        element.style.removeProperty('--fill');
      }
      element.title = room.shortDesc || '';
      var markersKey = room.exits.join(',') + '|' + (room.label || '') + '|' + (room.shortDesc || '');
      if (element.markersKey !== markersKey) {
        element.markersKey = markersKey;
        element.replaceChildren.apply(element, self.roomMarkers(room));
      }
    });
    this.roomEls.forEach(function (element, id) {
      if (!seen.has(id)) {
        element.remove();
        self.roomEls.delete(id);
      }
    });
  };

  MapView.prototype.roomMarkers = function (room) {
    var exits = new Set(room.exits);
    var markers = COMPASS_DIRS.filter(function (dir) { return exits.has(dir); }).map(function (dir) {
      return el('i', 'm chev-' + dir);
    });
    var name = el('div', 'name');
    name.append(el('span', '', room.shortDesc || ''));
    markers.push(name);
    var badges = el('div', 'badges');
    if (exits.has('u')) badges.append(el('span', 'badge', '▲'));
    if (exits.has('d')) badges.append(el('span', 'badge', '▼'));
    if (exits.has('special')) badges.append(el('span', 'badge', '◆'));
    if (badges.childElementCount) markers.push(badges);
    if (room.label) markers.push(el('span', 'label', room.label));
    return markers;
  };

  MapView.prototype.renderRoomPositions = function () {
    var self = this;
    this.roomById.forEach(function (room, id) {
      var element = self.roomEls.get(id);
      if (element) {
        element.style.left = room.x + 'px';
        element.style.top = room.y + 'px';
      }
    });
  };

  function edgeColor(room) {
    return room.indoors ? 'var(--edge-indoor)' : 'var(--edge-outdoor)';
  }

  MapView.prototype.renderEdges = function () {
    var self = this;
    var seen = new Set();
    var directions = new Set();
    this.state.exits.forEach(function (exit) { directions.add(exit.from + '\u0000' + exit.to); });
    this.state.exits.forEach(function (exit) {
      var from = self.roomById.get(exit.from);
      var to = self.roomById.get(exit.to);
      if (!from || !to) return;
      seen.add(exit.id);
      var loop = from === to;
      var kind = loop ? 'loop' : 'edge';
      var edge = self.edgeEls.get(exit.id);
      if (!edge || edge.kind !== kind) {
        if (edge) edge.remove();
        edge = el('div', kind);
        edge.kind = kind;
        edge.dataset.exit = exit.id;
        self.edgeLayer.append(edge);
        self.edgeEls.set(exit.id, edge);
      }
      var label = self.labelEls.get(exit.id);
      if (!label) {
        label = el('div', 'exit-label');
        label.dataset.exit = exit.id;
        self.labelLayer.append(label);
        self.labelEls.set(exit.id, label);
      }
      var picked = self.pickedExit === exit.id;
      var hover = self.hoverExit === exit.id;
      edge.className = kind + (picked ? ' picked' : '') + (hover ? ' hover' : '');
      edge.style.setProperty('--c1', edgeColor(from));
      edge.style.setProperty('--c2', edgeColor(to));
      edge.style.left = from.x + 'px';
      edge.style.top = from.y + 'px';
      var labelX, labelY;
      if (loop) {
        labelX = from.x - 52;
        labelY = from.y - 92;
      } else {
        var dx = to.x - from.x;
        var dy = to.y - from.y;
        var length = Math.hypot(dx, dy) || 1;
        var lane = directions.has(exit.to + '\u0000' + exit.from) ? EDGE_LANE_OFFSET : 0;
        edge.style.width = length + 'px';
        // positive y in the rotated line is the right hand side of the way the exit goes
        edge.style.transform = 'rotate(' + Math.atan2(dy, dx) + 'rad) translateY(' + lane + 'px)';
        // between the room and the arrow, so labels of exits going both ways don't overlap
        labelX = from.x + dx * 0.36 - dy / length * lane * 3;
        labelY = from.y + dy * 0.36 + dx / length * lane * 3;
      }
      label.textContent = exit.name;
      label.title = exit.name;
      label.className = 'exit-label' + (COMPASS_EXIT_NAMES.has(exit.name.toLowerCase()) ? ' minor' : '') +
        (exit.current ? ' current' : '') + (picked ? ' picked' : '') + (hover ? ' hover' : '');
      label.style.left = labelX + 'px';
      label.style.top = labelY + 'px';
    });
    [this.edgeEls, this.labelEls].forEach(function (elements) {
      elements.forEach(function (element, id) {
        if (!seen.has(id)) {
          element.remove();
          elements.delete(id);
        }
      });
    });
  };

  MapView.prototype.setHoverExit = function (id) {
    if (this.hoverExit === id) return;
    var self = this;
    [this.hoverExit, id].forEach(function (exitId) {
      if (exitId === null) return;
      var on = exitId === id;
      var edge = self.edgeEls.get(exitId);
      var label = self.labelEls.get(exitId);
      if (edge) edge.classList.toggle('hover', on);
      if (label) label.classList.toggle('hover', on);
    });
    this.hoverExit = id;
  };

  MapView.prototype.renderSide = function () {
    var state = this.state;
    var selected = state.selected;
    this.roomCard.classList.toggle('has-room', !!selected);
    this.shortDesc.textContent = selected ? selected.shortDesc || 'Unknown room' : '';
    this.longDesc.textContent = selected ? selected.longDesc || '' : '';
    var exitChips = selected && selected.exits ? selected.exits.split(', ').filter(Boolean).map(function (exit) {
      return el('span', 'chip', exit);
    }) : [];
    this.roomExits.replaceChildren.apply(this.roomExits, exitChips);

    var selectedId = selected ? selected.id : null;
    if (selectedId !== this.notesRoom) {
      this.flushNotes();
      this.notesRoom = selectedId;
      this.notes.value = selected ? selected.notes || '' : '';
    } else if (selected && document.activeElement !== this.notes && !this.notesTimer) {
      this.notes.value = selected.notes || '';
    }
    this.notes.disabled = !selected;

    var colorsKey = JSON.stringify(state.colors);
    if (colorsKey !== this.colorsKey) {
      this.colorsKey = colorsKey;
      var swatches = state.colors.map(function (color, index) {
        var swatch = el('button', 'swatch' + (color.color ? '' : ' normal'));
        swatch.type = 'button';
        swatch.dataset.index = String(index);
        swatch.title = color.name;
        swatch.setAttribute('aria-label', color.name);
        if (color.color) swatch.style.setProperty('--swatch', prettyColor(color.color));
        return swatch;
      });
      this.swatches.replaceChildren.apply(this.swatches, swatches);
    }
    var pickedCount = state.rooms.filter(function (room) { return room.picked; }).length;
    Array.prototype.forEach.call(this.swatches.children, function (swatch) {
      swatch.setAttribute('aria-pressed', String(!!selected && Number(swatch.dataset.index) === selected.colorIndex));
      swatch.disabled = pickedCount === 0;
    });

    this.toggles.snap.setAttribute('aria-pressed', String(state.snap));
    this.toggles.maze.setAttribute('aria-pressed', String(state.maze));
    this.toggles.revs.setAttribute('aria-pressed', String(state.revs));
    this.saveButton.disabled = !state.area || !state.canSave;
    this.saveButton.title = state.canSave ? 'Save the map to a file.' : 'This area is open in the mapper, it saves the area';
    this.clearButton.disabled = !state.area;
  };

  MapView.prototype.renderSearch = function () {
    var search = this.state.search;
    if (!search) return;
    if (document.activeElement !== this.searchText) this.searchText.value = search.text;

    var resultsKey = JSON.stringify(search.results);
    if (resultsKey !== this.resultsKey) {
      this.resultsKey = resultsKey;
      var first = el('option', '', search.text ? 'results (' + search.results.length + ')' : 'results');
      first.value = '';
      var options = [first].concat(search.results.map(function (result, index) {
        var option = el('option', '', result.area + ' - ' + result.shortDesc);
        option.value = String(index);
        return option;
      }));
      this.searchResults.replaceChildren.apply(this.searchResults, options);
    }

    var areasKey = JSON.stringify(search.areas);
    if (areasKey !== this.areasKey) {
      this.areasKey = areasKey;
      var firstArea = el('option', '', 'Areas list');
      firstArea.value = '';
      var areaOptions = [firstArea].concat(search.areas.map(function (area) {
        var option = el('option', '', area);
        option.value = area;
        return option;
      }));
      var previous = this.areaList.value;
      this.areaList.replaceChildren.apply(this.areaList, areaOptions);
      this.areaList.value = previous;
    }
  };

  MapView.prototype.renderEmptyNote = function () {
    var state = this.state;
    var text = '';
    if (state.rooms.length === 0) {
      if (this.name === 'search') {
        text = 'Search for a room or pick an area to view its map.';
      } else if (!state.area) {
        text = 'Not in a mapped area. The map draws itself as you walk around in an area.';
      } else {
        text = 'Nothing mapped in this area yet, walk around to map it.';
      }
    }
    this.emptyNote.textContent = text;
    this.emptyNote.hidden = !text;
  };

  // ---- keeping the current room in view ----

  MapView.prototype.isVisible = function () {
    return !this.root.hidden && this.viewport.clientWidth > 0;
  };

  MapView.prototype.centerOn = function (x, y) {
    this.tx = this.viewport.clientWidth / 2 - x * this.scale;
    this.ty = this.viewport.clientHeight / 2 - y * this.scale;
    this.applyTransform();
  };

  MapView.prototype.centerOnAllRooms = function () {
    var rooms = this.state.rooms;
    if (rooms.length === 0) return;
    var xs = rooms.map(function (room) { return room.x; });
    var ys = rooms.map(function (room) { return room.y; });
    this.centerOn((Math.min.apply(null, xs) + Math.max.apply(null, xs)) / 2,
      (Math.min.apply(null, ys) + Math.max.apply(null, ys)) / 2);
  };

  // like the old mapper: recenter when the current room drifts too far from the middle of the view
  MapView.prototype.needsRecenter = function (room) {
    var cx = this.viewport.clientWidth / 2;
    var cy = this.viewport.clientHeight / 2;
    var x = room.x * this.scale + this.tx;
    var y = room.y * this.scale + this.ty;
    return x < 0.6 * cx || x > 1.3 * cx || y < 0.6 * cy || y > 1.3 * cy;
  };

  MapView.prototype.follow = function () {
    if (!this.state || !this.isVisible()) return;
    var current = this.state.rooms.find(function (room) { return room.current; });
    if (this.state.area !== this.lastArea) {
      this.lastArea = this.state.area;
      this.lastCurrentKey = current ? current.id + ':' + current.x + ':' + current.y : null;
      if (current) {
        this.centerOn(current.x, current.y);
      } else {
        this.centerOnAllRooms();
      }
      return;
    }
    if (!current) return;
    var key = current.id + ':' + current.x + ':' + current.y;
    if (key !== this.lastCurrentKey) {
      this.lastCurrentKey = key;
      if (this.needsRecenter(current)) this.centerOn(current.x, current.y);
    }
  };

  MapView.prototype.zoom = function (factor, sx, sy) {
    var newScale = Math.min(MAX_SCALE, Math.max(MIN_SCALE, this.scale * factor));
    var wx = (sx - this.tx) / this.scale;
    var wy = (sy - this.ty) / this.scale;
    this.scale = newScale;
    this.tx = sx - wx * this.scale;
    this.ty = sy - wy * this.scale;
    this.applyTransform();
    storageSet('batmap.scale.' + this.name, String(this.scale));
  };

  MapView.prototype.zoomAtCenter = function (factor) {
    this.zoom(factor, this.viewport.clientWidth / 2, this.viewport.clientHeight / 2);
  };

  // ---- mouse handling ----

  MapView.prototype.screenPoint = function (e) {
    var rect = this.viewport.getBoundingClientRect();
    return { x: e.clientX - rect.left, y: e.clientY - rect.top };
  };

  MapView.prototype.toWorld = function (p) {
    return { x: (p.x - this.tx) / this.scale, y: (p.y - this.ty) / this.scale };
  };

  MapView.prototype.setLocalPicks = function (pickedId) {
    this.roomById.forEach(function (room) { room.picked = room.id === pickedId; });
    this.renderRooms();
  };

  // ends a drag without doing anything, for when its pointerup never arrives, like when a dialog opens
  MapView.prototype.cancelDrag = function () {
    this.drag = null;
    this.viewport.classList.remove('panning');
    this.selectRect.hidden = true;
    this.applyPendingState();
  };

  MapView.prototype.applyPendingState = function () {
    if (this.pendingState) {
      var state = this.pendingState;
      this.pendingState = null;
      this.apply(state);
    }
  };

  MapView.prototype.onPointerDown = function (e) {
    if (!this.state || (e.button !== 0 && e.button !== 2)) return;
    if (this.drag) this.cancelDrag();
    this.suppressContextMenu = false;
    this.viewport.focus({ preventScroll: true });
    var roomEl = e.target.closest('.room');
    var exitEl = e.target.closest('[data-exit]');
    var p = this.screenPoint(e);

    if (roomEl) {
      if (e.button !== 0) return;
      var id = roomEl.dataset.id;
      var room = this.roomById.get(id);
      if (!room) return;
      if (e.ctrlKey || e.metaKey) {
        this.showDirs(room);
        return;
      }
      if (e.shiftKey) {
        room.picked = !room.picked;
        this.renderRooms();
        this.act('pick', { room: id, toggle: true });
        return;
      }
      var wasPicked = room.picked;
      if (!wasPicked) {
        this.setLocalPicks(id);
        this.act('pick', { room: id });
      }
      var origin = new Map();
      this.roomById.forEach(function (r) {
        if (r.picked) origin.set(r.id, { x: r.x, y: r.y });
      });
      this.drag = { mode: 'rooms', id: id, wasPicked: wasPicked, start: p, origin: origin, moved: false };
    } else if (e.button === 2 || e.shiftKey) {
      this.drag = { mode: 'rect', start: p, moved: false };
    } else {
      this.drag = { mode: 'pan', start: p, tx: this.tx, ty: this.ty, moved: false, onExit: !!exitEl };
      this.viewport.classList.add('panning');
    }
    this.drag.pointerId = e.pointerId;
  };

  MapView.prototype.onPointerMove = function (e) {
    var drag = this.drag;
    if (!drag) return;
    var p = this.screenPoint(e);
    var dx = p.x - drag.start.x;
    var dy = p.y - drag.start.y;
    if (!drag.moved) {
      if (Math.abs(dx) < DRAG_THRESHOLD && Math.abs(dy) < DRAG_THRESHOLD) return;
      drag.moved = true;
      // captured only once dragging, so clicks and double-clicks still go to the exit or room under the mouse
      try {
        this.viewport.setPointerCapture(drag.pointerId);
      } catch (err) { /* pointer already gone, dragging still works while over the map */ }
    }

    if (drag.mode === 'rooms') {
      var wdx = dx / this.scale;
      var wdy = dy / this.scale;
      if (this.state.snap) {
        wdx = Math.round(wdx / ROOM_GRID) * ROOM_GRID;
        wdy = Math.round(wdy / ROOM_GRID) * ROOM_GRID;
      }
      var self = this;
      drag.origin.forEach(function (position, id) {
        var room = self.roomById.get(id);
        if (room) {
          room.x = position.x + wdx;
          room.y = position.y + wdy;
        }
      });
      this.renderRoomPositions();
      this.renderEdges();
    } else if (drag.mode === 'pan') {
      this.tx = drag.tx + dx;
      this.ty = drag.ty + dy;
      this.applyTransform();
    } else if (drag.mode === 'rect') {
      var r = this.selectRect.style;
      r.left = Math.min(p.x, drag.start.x) + 'px';
      r.top = Math.min(p.y, drag.start.y) + 'px';
      r.width = Math.abs(dx) + 'px';
      r.height = Math.abs(dy) + 'px';
      this.selectRect.hidden = false;
    }
  };

  MapView.prototype.onPointerUp = function (e) {
    var drag = this.drag;
    if (!drag) return;
    this.drag = null;
    this.viewport.classList.remove('panning');
    this.selectRect.hidden = true;
    var self = this;

    if (drag.mode === 'rooms') {
      var positions = {};
      var changed = false;
      drag.origin.forEach(function (position, id) {
        var room = self.roomById.get(id);
        if (room && (room.x !== position.x || room.y !== position.y)) {
          positions[id] = { x: room.x, y: room.y };
          changed = true;
        }
      });
      if (changed) {
        // keep showing the moved positions until the server has them
        this.positionOverrides = new Map(Object.keys(positions).map(function (id) { return [id, positions[id]]; }));
        this.overrideUntilVersion = Infinity;
        this.act('move', { positions: positions }).then(function (response) {
          self.overrideUntilVersion = response.version;
        }, function () {
          self.positionOverrides = null;
        });
      } else if (!drag.moved && drag.wasPicked) {
        this.setLocalPicks(drag.id);
        this.act('pick', { room: drag.id });
      }
    } else if (drag.mode === 'pan') {
      if (!drag.moved && !drag.onExit) {
        this.setLocalPicks(null);
        this.act('pick', { room: null });
      }
    } else if (drag.mode === 'rect' && drag.moved) {
      // on some systems the context menu event comes after the button is released
      this.suppressContextMenu = e.button === 2;
      var end = this.screenPoint(e);
      var a = this.toWorld(drag.start);
      var b = this.toWorld(end);
      var minX = Math.min(a.x, b.x), maxX = Math.max(a.x, b.x);
      var minY = Math.min(a.y, b.y), maxY = Math.max(a.y, b.y);
      var ids = [];
      this.roomById.forEach(function (room) {
        if (room.x >= minX && room.x <= maxX && room.y >= minY && room.y <= maxY) {
          room.picked = true;
          ids.push(room.id);
        }
      });
      this.renderRooms();
      if (ids.length) this.act('pickMany', { rooms: ids, clear: false });
    }

    this.applyPendingState();
  };

  MapView.prototype.onContextMenu = function (e) {
    e.preventDefault();
    if (!this.state) return;
    if (this.drag) {
      // a right-drag selecting rooms ends in a context menu event too, that one is not a click
      if (this.drag.moved) return;
      this.cancelDrag();
    }
    if (this.suppressContextMenu) {
      this.suppressContextMenu = false;
      return;
    }
    var roomEl = e.target.closest('.room');
    var exitEl = e.target.closest('[data-exit]');
    if (roomEl) {
      var room = this.roomById.get(roomEl.dataset.id);
      if (room && room.picked) this.askDeleteRooms(room);
    } else if (exitEl) {
      this.askDeleteExit(Number(exitEl.dataset.exit));
    }
  };

  MapView.prototype.onDoubleClick = function (e) {
    if (!this.state) return;
    var exitEl = e.target.closest('[data-exit]');
    if (exitEl) {
      this.askRenameExit(Number(exitEl.dataset.exit));
    } else if (!e.target.closest('.room')) {
      this.root.classList.toggle('no-side');
      storageSet('batmap.side.' + this.name, this.root.classList.contains('no-side') ? 'hidden' : 'shown');
    }
  };

  MapView.prototype.findExit = function (id) {
    return this.state.exits.find(function (exit) { return exit.id === id; });
  };

  MapView.prototype.withPickedExit = function (id, promise) {
    var self = this;
    this.pickedExit = id;
    this.renderEdges();
    return promise.then(function (result) {
      self.pickedExit = null;
      if (self.state) self.renderEdges();
      return result;
    });
  };

  MapView.prototype.askDeleteRooms = function (room) {
    var self = this;
    var picked = this.state.rooms.filter(function (r) { return r.picked; });
    var what = picked.length > 1 ? picked.length + ' rooms' : room.shortDesc || 'this room';
    modal.confirm('Room delete', 'Do you wish to delete?\n' + what).then(function (yes) {
      if (yes) self.act('deleteRooms');
    });
  };

  MapView.prototype.askDeleteExit = function (id) {
    var self = this;
    var exit = this.findExit(id);
    if (!exit) return;
    this.withPickedExit(id, modal.confirm('Exit delete', 'Do you wish to delete this exit?\n' + exit.name)).then(function (yes) {
      if (yes) self.act('deleteExit', { exit: id });
    });
  };

  MapView.prototype.askRenameExit = function (id) {
    var self = this;
    var exit = this.findExit(id);
    if (!exit) return;
    var dialog = modal.open({
      title: 'Exit edit',
      body: 'What do you want to change the exit to?',
      input: exit.name,
      buttons: [{ label: 'Cancel', value: false }, { label: 'Rename', value: true, primary: true }]
    });
    this.withPickedExit(id, dialog).then(function (result) {
      var name = result.input.trim();
      if (result.value === true && name && name !== exit.name) self.act('renameExit', { exit: id, name: name });
    });
  };

  MapView.prototype.showDirs = function (room) {
    var self = this;
    this.act('dirs', { room: room.id }).then(function (response) {
      var title = 'Dirs to ' + (room.shortDesc || 'room');
      if (!response.dirs) {
        modal.open({ title: title, body: 'No known path there from the room you are in.', buttons: [{ label: 'Ok', value: null, primary: true }] });
        return;
      }
      modal.open({
        title: title,
        body: response.dirs,
        buttons: [
          { label: 'Ok', value: null, primary: true },
          { label: 'Send to mud', value: 'mud' },
          { label: 'Send to party', value: 'party' }
        ]
      }).then(function (result) {
        if (result.value === 'mud') self.act('sendToMud', { text: response.dirs });
        if (result.value === 'party') self.act('sendToParty', { text: response.shortDirs });
      });
    }, function () { /* already reported */ });
  };

  MapView.prototype.flushNotes = function () {
    if (!this.notesTimer) return;
    clearTimeout(this.notesTimer);
    this.notesTimer = null;
    if (this.notesRoom !== null) this.act('notes', { room: this.notesRoom, notes: this.notes.value });
  };

  MapView.prototype.bindEvents = function () {
    var self = this;
    var vp = this.viewport;
    vp.addEventListener('pointerdown', function (e) { self.onPointerDown(e); });
    vp.addEventListener('pointermove', function (e) { self.onPointerMove(e); });
    vp.addEventListener('pointerup', function (e) { self.onPointerUp(e); });
    vp.addEventListener('pointercancel', function (e) { self.onPointerUp(e); });
    vp.addEventListener('contextmenu', function (e) { self.onContextMenu(e); });
    vp.addEventListener('dblclick', function (e) { self.onDoubleClick(e); });
    vp.addEventListener('wheel', function (e) {
      e.preventDefault();
      var p = self.screenPoint(e);
      self.zoom(e.deltaY < 0 ? ZOOM_STEP : 1 / ZOOM_STEP, p.x, p.y);
    }, { passive: false });
    vp.addEventListener('keydown', function (e) {
      if (e.key === '+' || e.key === '=') self.zoomAtCenter(ZOOM_STEP);
      if (e.key === '-') self.zoomAtCenter(1 / ZOOM_STEP);
    });

    vp.addEventListener('pointerover', function (e) {
      var exitEl = e.target.closest('[data-exit]');
      self.setHoverExit(exitEl ? Number(exitEl.dataset.exit) : null);
    });
    vp.addEventListener('pointerleave', function () { self.setHoverExit(null); });
    // the buttons floating on the map shouldn't start drags or clear the selection
    this.root.querySelector('.map-controls').addEventListener('pointerdown', function (e) { e.stopPropagation(); });
    this.root.querySelector('.map-controls').addEventListener('dblclick', function (e) { e.stopPropagation(); });

    this.root.querySelector('.zoom-in').addEventListener('click', function () { self.zoomAtCenter(ZOOM_STEP); });
    this.root.querySelector('.zoom-out').addEventListener('click', function () { self.zoomAtCenter(1 / ZOOM_STEP); });
    this.root.querySelector('.center-me').addEventListener('click', function () {
      if (!self.state) return;
      var current = self.state.rooms.find(function (room) { return room.current; });
      if (current) {
        self.centerOn(current.x, current.y);
      } else {
        self.centerOnAllRooms();
      }
    });
    this.swatches.addEventListener('click', function (e) {
      var swatch = e.target.closest('.swatch');
      if (swatch && !swatch.disabled) self.act('color', { index: Number(swatch.dataset.index) });
    });
    this.saveButton.addEventListener('click', function () { self.act('save'); });
    this.clearButton.addEventListener('click', function () {
      modal.confirm('Clear map', 'This will clear ALL data and notes associated with this area. Are you sure?').then(function (yes) {
        if (yes) self.act('clear');
      });
    });
    Object.keys(this.toggles).forEach(function (name) {
      var button = self.toggles[name];
      button.addEventListener('click', function () {
        var enabled = button.getAttribute('aria-pressed') !== 'true';
        button.setAttribute('aria-pressed', String(enabled));
        self.act(name, { enabled: enabled });
      });
    });

    this.notes.addEventListener('input', function () {
      clearTimeout(self.notesTimer);
      self.notesTimer = setTimeout(function () {
        self.notesTimer = null;
        if (self.notesRoom !== null) self.act('notes', { room: self.notesRoom, notes: self.notes.value });
      }, NOTES_SAVE_DELAY);
    });
    this.notes.addEventListener('blur', function () { self.flushNotes(); });

    this.searchText.addEventListener('keydown', function (e) {
      if (e.key === 'Enter') {
        e.preventDefault();
        self.act('search', { text: self.searchText.value });
      }
    });
    this.searchResults.addEventListener('change', function () {
      var search = self.state && self.state.search;
      var result = search && search.results[Number(self.searchResults.value)];
      if (self.searchResults.value !== '' && result) {
        self.lastCurrentKey = null;
        self.act('showRoom', { area: result.area, room: result.roomId });
      }
    });
    this.areaList.addEventListener('change', function () {
      if (self.areaList.value !== '') self.act('showRoom', { area: self.areaList.value, room: null });
    });
  };

  // ---- tabs ----

  function updateAreaName() {
    var state = activeView && activeView.state;
    var text;
    if (!state) {
      text = '';
    } else if (activeView.name === 'search') {
      text = state.area ? 'Viewing ' + state.area : '';
    } else {
      text = state.area || 'Not in a mapped area';
    }
    areaNameEl.textContent = text;
    document.title = state && state.area ? state.area + ' - batMap' : 'batMap';
  }

  function showView(name) {
    if (!views[name]) name = 'map';
    activeView = views[name];
    Object.keys(views).forEach(function (viewName) {
      views[viewName].root.hidden = viewName !== name;
    });
    document.querySelectorAll('.tab').forEach(function (tab) {
      tab.setAttribute('aria-selected', String(tab.dataset.view === name));
    });
    updateAreaName();
    activeView.follow();
  }

  views.map = new MapView('map');
  views.search = new MapView('search');
  document.querySelectorAll('.tab').forEach(function (tab) {
    tab.addEventListener('click', function () {
      history.replaceState(null, '', '#' + tab.dataset.view);
      showView(tab.dataset.view);
    });
  });
  window.addEventListener('hashchange', function () { showView(location.hash.slice(1)); });
  showView(location.hash.slice(1) || 'map');
})();

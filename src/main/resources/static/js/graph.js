// Renders the RDF graph emitted by UploadController using vis-network.
(function () {
    const raw = document.getElementById('graph-data').textContent;
    const data = JSON.parse(raw);
    const container = document.getElementById('graph-container');
    if (!container) return;

    // Pick a stable colour per group so nodes of the same rdf:type look alike.
    const palette = [
        '#7a4fa3', '#3a8dbd', '#dd7b3b', '#3da37c', '#c95a90',
        '#b29027', '#5e6dac', '#a1502e', '#4ea6a6', '#8a3f3f'
    ];
    const groupColor = {};
    let paletteIdx = 0;
    function colorFor(group) {
        if (group === 'literal') return '#cccccc';
        if (group === 'blank')   return '#e9e2d4';
        if (!(group in groupColor)) {
            groupColor[group] = palette[paletteIdx++ % palette.length];
        }
        return groupColor[group];
    }

    const nodes = data.nodes.map(n => ({
        id: n.id,
        label: n.label,
        title: n.id + (n.group ? '  (' + n.group + ')' : ''),
        shape: n.group === 'literal' ? 'box' : 'dot',
        size: n.group === 'literal' ? 12 : 18,
        color: { background: colorFor(n.group), border: '#1f1d1b' },
        font: { color: n.group === 'literal' ? '#1f1d1b' : '#1f1d1b', size: 14 }
    }));

    const edges = data.edges.map((e, i) => ({
        id: 'e' + i,
        from: e.from,
        to: e.to,
        label: e.label,
        arrows: 'to',
        font: { align: 'middle', size: 11, color: '#6b675f' },
        color: { color: '#a8a39a' },
        smooth: { type: 'continuous' }
    }));

    const network = new vis.Network(container, { nodes, edges }, {
        layout:    { improvedLayout: true },
        physics:   { stabilization: { iterations: 200 } },
        edges:     { length: 180 },
        nodes:     { borderWidth: 1 },
        interaction:{ hover: true, tooltipDelay: 100 }
    });

    // Build a small legend so reviewers can tell groups apart.
    const legend = document.createElement('div');
    legend.id = 'graph-legend';
    Object.entries(groupColor).forEach(([group, color]) => {
        const item = document.createElement('span');
        item.innerHTML = `<i style="background:${color}"></i>${group}`;
        legend.appendChild(item);
    });
    container.parentNode.insertBefore(legend, container.nextSibling);
})();

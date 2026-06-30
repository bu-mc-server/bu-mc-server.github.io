d3.json("map_data.json").then(data => {
    const svg = d3.select("#map-svg");
    const g = svg.append("g");

    const zoom = d3.zoom()
        .scaleExtent([0.1, 10])
        .on("zoom", (event) => g.attr("transform", event.transform));

    svg.call(zoom);

    const padding = 200;
    const viewBoxStr = `${data.minX - padding} ${data.minZ - padding} ${data.maxX - data.minX + 2*padding} ${data.maxZ - data.minZ + 2*padding}`;
    svg.attr("viewBox", viewBoxStr);

    // 1. Draw Lines
    data.lines.forEach(line => {
        g.append("g")
            .attr("class", "transit-line")
            .selectAll("line")
            .data(line.segments)
            .join("line")
            .attr("class", "transit-segment")
            .attr("x1", d => d.x1)
            .attr("y1", d => d.z1)
            .attr("x2", d => d.x2)
            .attr("y2", d => d.z2)
            .attr("stroke", line.color)
            .attr("stroke-width", 12)
            .attr("stroke-linecap", "round");
    });

    // 2. Draw Stations (Grouped to allow multiple circles per station)
    const stationNodes = g.selectAll(".station-group")
        .data(data.stations)
        .join("g")
        .attr("class", "station-group")
        .attr("transform", d => `translate(${d.x}, ${d.z})`)
        .style("cursor", "pointer");

    let selectedStationNode = null;

    function clearSelection() {
        if (selectedStationNode) {
            const node = d3.select(selectedStationNode);
            const d = node.datum();
            node.select("circle").attr("stroke", d.color).attr("stroke-width", 4);
            selectedStationNode = null;
        }
    }

    // Outer colored ring
    stationNodes.append("circle")
        .attr("class", "station-ring")
        .attr("r", 10)
        .attr("fill", "#1e1e1e")
        .attr("stroke", d => d.color)
        .attr("stroke-width", 4);

// Hover effect (ignores the currently selected node)
    stationNodes.on("mouseover", function() {
        if (this !== selectedStationNode) {
            d3.select(this).select("circle").attr("stroke", "white").attr("stroke-width", 6);
        }
    }).on("mouseout", function(event, d) {
        if (this !== selectedStationNode) {
            d3.select(this).select("circle").attr("stroke", d.color).attr("stroke-width", 4);
        }
    });

    // 3. Build Attached SVG Tooltip
    const svgTooltip = g.append("g").attr("id", "svg-tooltip").style("display", "none");
    const tooltipBg = svgTooltip.append("rect").attr("fill", "rgba(0,0,0,0.85)").attr("stroke", "#555").attr("rx", 8);
    const tTitle = svgTooltip.append("text").attr("fill", "white").attr("font-size", "22px").attr("font-weight", "bold");
    const tLine = svgTooltip.append("text").attr("font-size", "16px");
    const tDiv = svgTooltip.append("text").attr("fill", "#ccc").attr("font-size", "16px");
    const tCoords = svgTooltip.append("text").attr("fill", "#ccc").attr("font-size", "16px");
    const tClose = svgTooltip.append("text").attr("fill", "#aaa").attr("font-size", "24px").attr("font-weight", "bold")
        .text("×").style("cursor", "pointer")
        .on("click", (e) => { e.stopPropagation(); svgTooltip.style("display", "none"); });

    function showAttachedPopup(d) {
        svgTooltip.style("display", null);
        tTitle.text(d.name).attr("x", 15).attr("y", 30);

        // Handle black text visibility
        const displayColor = (d.color === "#000000") ? "white" : d.color;

        tLine.text("Line: " + d.lineName).attr("fill", displayColor).attr("x", 15).attr("y", 55);
        tDiv.text("Division: " + d.division).attr("x", 15).attr("y", 75);
        tCoords.text(`Coordinates: (${d.x}, ${d.z})`).attr("x", 15).attr("y", 95);

        let maxTextW = Math.max(tTitle.node().getComputedTextLength(), tLine.node().getComputedTextLength(),
            tDiv.node().getComputedTextLength(), tCoords.node().getComputedTextLength());
        let w = Math.max(maxTextW + 40, 200);
        let h = 110;

        tooltipBg.attr("width", w).attr("height", h);
        tClose.attr("x", w - 25).attr("y", 25);

        // Spatial Collision Detection
        function hasIntersection(bx, by) {
            return data.stations.some(s => {
                return (s.x + 10 >= bx && s.x - 10 <= bx + w) && (s.z + 10 >= by && s.z - 10 <= by + h);
            });
        }

        const offset = 20;
        let boxX = d.x + offset;
        let boxY = d.z - h/2; // Try Right Side

        if (hasIntersection(boxX, boxY)) {
            let leftX = d.x - w - offset;
            if (!hasIntersection(leftX, boxY)) {
                boxX = leftX; // Fallback to Left
            } else {
                let topY = d.z - h - offset;
                let centerX = d.x - w/2;
                if (!hasIntersection(centerX, topY)) {
                    boxX = centerX; boxY = topY; // Fallback to Top
                } else {
                    let botY = d.z + offset;
                    if (!hasIntersection(centerX, botY)) {
                        boxX = centerX; boxY = botY; // Fallback to Bottom
                    }
                    // If fully boxed in, default to Right side overlapping
                }
            }
        }

        svgTooltip.attr("transform", `translate(${boxX}, ${boxY})`);
    }

    // 4. Click Events
    stationNodes.on("click", function(event, d) {
        clearSelection(); // Clear previous selection

        selectedStationNode = this;
        d3.select(this).select("circle").attr("stroke", "white").attr("stroke-width", 6);

        const mode = document.getElementById("popup-mode").value;
        const displayColor = (d.color === "#000000") ? "white" : d.color;

        if (mode === "FIXED") {
            svgTooltip.style("display", "none");
            document.getElementById("p-name").innerText = d.name;
            document.getElementById("p-line").innerText = d.lineName;
            document.getElementById("p-line").style.color = displayColor;
            document.getElementById("p-div").innerText = d.division;
            document.getElementById("p-coords").innerText = `(${d.x}, ${d.z})`;
            document.getElementById("popup").style.display = "block";
        } else {
            document.getElementById("popup").style.display = "none";
            showAttachedPopup(d);
        }
    });

    document.getElementById("close-btn").onclick = () => {
        document.getElementById("popup").style.display = "none";
        clearSelection();
    };

    // Modular Filtering
    document.getElementById("division-select").addEventListener("change", (e) => {
        const selected = e.target.value;

        stationNodes.style("display", d => (selected === "ALL" || d.division === selected) ? null : "none");
        g.selectAll(".transit-segment").style("display", d => (selected === "ALL" || d.division === selected) ? null : "none");

        document.getElementById("popup").style.display = "none";
        svgTooltip.style("display", "none");
        clearSelection(); // Remove highlight when swapping maps
    });

}).catch(err => console.error("Error loading map data:", err));
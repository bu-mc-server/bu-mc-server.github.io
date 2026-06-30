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

    data.lines.forEach(line => {
        g.append("g")
            .attr("class", "transit-line")
            .selectAll("line")
            .data(line.segments)
            .join("line") // Replaces enter().append()
            .attr("x1", d => d.x1)
            .attr("y1", d => d.z1)
            .attr("x2", d => d.x2)
            .attr("y2", d => d.z2)
            .attr("stroke", line.color)
            .attr("stroke-width", 12)
            .attr("stroke-linecap", "round");
    });

    const stations = g.selectAll("circle")
        .data(data.stations)
        .join("circle") // Replaces enter().append()
        .attr("class", "station")
        .attr("cx", d => d.x)
        .attr("cy", d => d.z)
        .attr("r", 10)
        .attr("fill", "#1e1e1e")
        .attr("stroke", d => d.color)
        .attr("stroke-width", 4)
        .on("click", (event, d) => {
            document.getElementById("p-name").innerText = d.name;
            document.getElementById("p-line").innerText = d.color;
            document.getElementById("p-line").style.color = d.color;
            document.getElementById("p-div").innerText = d.division;
            document.getElementById("p-coords").innerText = `(${d.x}, ${d.z})`;
            document.getElementById("popup").style.display = "block";
        });

    document.getElementById("close-btn").onclick = () => {
        document.getElementById("popup").style.display = "none";
    };

    document.getElementById("division-select").addEventListener("change", (e) => {
        const selected = e.target.value;
        stations.style("opacity", d => (selected === "ALL" || d.division === selected) ? 1 : 0.1);
    });
}).catch(err => console.error("Error loading map data:", err));
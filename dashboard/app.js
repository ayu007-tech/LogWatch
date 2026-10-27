const API = "http://localhost:8080/api";

let allLogs = [];
let logChart = null;


/* ==========================================
   HELPERS
========================================== */

function escapeHTML(value) {

    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}


function showToast(message) {

    const toast =
        document.getElementById("toast");

    toast.textContent = message;

    toast.classList.add("show");

    setTimeout(() => {
        toast.classList.remove("show");
    }, 2500);
}


async function apiGet(endpoint) {

    const response =
        await fetch(`${API}${endpoint}`);

    if (!response.ok) {
        throw new Error(
            `API request failed: ${response.status}`
        );
    }

    return response.json();
}


/* ==========================================
   CLOCK
========================================== */

function updateClock() {

    const now = new Date();

    document.getElementById("clock")
        .textContent =
        now.toLocaleTimeString();
}

setInterval(updateClock, 1000);

updateClock();


/* ==========================================
   API CONNECTION
========================================== */

function setConnection(online) {

    const dot =
        document.getElementById("connectionDot");

    const text =
        document.getElementById("connectionText");

    if (online) {

        dot.className =
            "connection-dot online";

        text.textContent =
            "API ONLINE";

    } else {

        dot.className =
            "connection-dot offline";

        text.textContent =
            "API OFFLINE";
    }
}


/* ==========================================
   STATISTICS
========================================== */

async function loadStatistics() {

    const stats =
        await apiGet("/statistics");

    document.getElementById("totalLogs")
        .textContent =
        stats.totalLogs ?? 0;

    document.getElementById("infoLogs")
        .textContent =
        stats.info ?? 0;

    document.getElementById("warningLogs")
        .textContent =
        stats.warnings ?? 0;

    document.getElementById("errorLogs")
        .textContent =
        stats.errors ?? 0;

    document.getElementById("exceptionLogs")
        .textContent =
        stats.exceptions ?? 0;
}


/* ==========================================
   HEALTH
========================================== */

async function loadHealth() {

    const health =
        await apiGet("/health");

    const score =
        Number(health.score ?? 0);

    document.getElementById("healthScore")
        .textContent = score;

    const status =
        document.getElementById("healthStatus");

    status.textContent =
        health.status ?? "UNKNOWN";


    const ring =
        document.getElementById("healthRing");

    const circumference = 314;

    const offset =
        circumference -
        (score / 100) * circumference;

    ring.style.strokeDashoffset =
        offset;


    if (score >= 80) {

        ring.style.stroke =
            "var(--cyan)";

        status.style.color =
            "var(--cyan)";

        document.getElementById("healthMessage")
            .textContent =
            "System operating normally";

    } else if (score >= 60) {

        ring.style.stroke =
            "var(--yellow)";

        status.style.color =
            "var(--yellow)";

        document.getElementById("healthMessage")
            .textContent =
            "Attention recommended";

    } else {

        ring.style.stroke =
            "var(--red)";

        status.style.color =
            "var(--red)";

        document.getElementById("healthMessage")
            .textContent =
            "Critical system condition";
    }
}


/* ==========================================
   LOGS
========================================== */

async function loadLogs() {

    const logs =
        await apiGet("/logs");

    allLogs = Array.isArray(logs)
        ? logs
        : [];

    renderLogs();
    updateChart();
}


function getFilteredLogs() {

    const search =
        document.getElementById("searchInput")
            .value
            .toLowerCase()
            .trim();

    const level =
        document.getElementById("levelFilter")
            .value;


    return allLogs.filter(log => {

        const matchesLevel =
            level === "ALL" ||
            String(log.level)
                .toUpperCase() === level;


        const searchable =
            `${log.timestamp || ""}
             ${log.level || ""}
             ${log.message || ""}`
                .toLowerCase();


        const matchesSearch =
            !search ||
            searchable.includes(search);


        return matchesLevel &&
            matchesSearch;
    });
}


function renderLogs() {

    const tbody =
        document.getElementById("logTableBody");

    const logs =
        getFilteredLogs()
            .slice()
            .reverse();


    if (!logs.length) {

        tbody.innerHTML = `
            <tr>
                <td colspan="3" class="no-data">
                    No logs found.
                </td>
            </tr>
        `;

        return;
    }


    tbody.innerHTML =
        logs.map(log => {

            const level =
                String(log.level || "INFO")
                    .toUpperCase();


            let levelClass =
                "level-info";

            if (level === "WARNING") {
                levelClass =
                    "level-warning";
            }

            if (level === "ERROR") {
                levelClass =
                    "level-error";
            }

            if (level === "EXCEPTION") {
                levelClass =
                    "level-exception";
            }


            return `
                <tr>

                    <td>
                        ${escapeHTML(
                            formatTimestamp(
                                log.timestamp
                            )
                        )}
                    </td>

                    <td>
                        <span class="level ${levelClass}">
                            ${escapeHTML(level)}
                        </span>
                    </td>

                    <td>
                        ${escapeHTML(log.message)}
                    </td>

                </tr>
            `;

        }).join("");
}


function formatTimestamp(timestamp) {

    if (!timestamp) {
        return "--";
    }

    try {

        const date =
            new Date(timestamp);

        if (isNaN(date.getTime())) {
            return timestamp;
        }

        return date.toLocaleString();

    } catch {
        return timestamp;
    }
}


/* ==========================================
   CHART
========================================== */

function updateChart() {

    const canvas =
        document.getElementById("logChart");

    if (!canvas) {
        return;
    }


    const labels = [];
    const values = [];


    const recent =
        allLogs.slice(-15);


    recent.forEach((log, index) => {

        labels.push(
            index + 1
        );

        values.push(1);
    });


    if (logChart) {
        logChart.destroy();
    }


    logChart =
        new Chart(canvas, {

            type: "line",

            data: {

                labels,

                datasets: [

                    {
                        label: "Log Events",

                        data: values,

                        borderColor:
                            "#00ffd0",

                        backgroundColor:
                            "rgba(0,255,208,0.08)",

                        borderWidth: 2,

                        fill: true,

                        tension: 0.4,

                        pointRadius: 2,

                        pointBackgroundColor:
                            "#00ffd0"
                    }

                ]
            },

            options: {

                responsive: true,

                maintainAspectRatio: false,

                plugins: {

                    legend: {
                        display: false
                    }
                },

                scales: {

                    x: {

                        grid: {
                            color:
                                "rgba(255,255,255,0.04)"
                        },

                        ticks: {
                            color: "#59646d",
                            font: {
                                size: 8
                            }
                        }
                    },

                    y: {

                        beginAtZero: true,

                        grid: {
                            color:
                                "rgba(255,255,255,0.04)"
                        },

                        ticks: {
                            color: "#59646d",
                            font: {
                                size: 8
                            },
                            precision: 0
                        }
                    }
                }
            }
        });
}


/* ==========================================
   TOP ERRORS
========================================== */

async function loadErrors() {

    const errors =
        await apiGet("/errors");

    const container =
        document.getElementById("topErrors");


    if (!errors || !errors.length) {

        container.innerHTML = `
            <div class="no-data">
                No errors detected.
            </div>
        `;

        return;
    }


    container.innerHTML =
        errors.map(error => {

            return `
                <div class="error-item">

                    <span class="error-message">
                        ${escapeHTML(
                            error.message
                        )}
                    </span>

                    <span class="error-count">
                        ×${escapeHTML(
                            error.count
                        )}
                    </span>

                </div>
            `;

        }).join("");
}


/* ==========================================
   ALERTS
========================================== */

async function loadAlerts() {

    const alerts =
        await apiGet("/alerts");

    const container =
        document.getElementById("alertsList");


    if (!alerts || !alerts.length) {

        container.innerHTML = `
            <div class="no-data">
                ✓ No active alerts.
            </div>
        `;

        return;
    }


    container.innerHTML =
        alerts.map(alert => {

            return `
                <div class="alert-item">

                    <strong>
                        ${escapeHTML(
                            alert.severity ||
                            "ALERT"
                        )}
                    </strong>

                    <span>
                        ${escapeHTML(
                            alert.message ||
                            alert.type ||
                            "System alert"
                        )}
                    </span>

                </div>
            `;

        }).join("");
}


/* ==========================================
   DASHBOARD REFRESH
========================================== */

async function refreshDashboard() {

    try {

        await Promise.all([

            loadStatistics(),

            loadHealth(),

            loadLogs(),

            loadErrors(),

            loadAlerts()

        ]);

        setConnection(true);

    } catch (error) {

        console.error(
            "Dashboard refresh failed:",
            error
        );

        setConnection(false);
    }
}


/* ==========================================
   SINGLE LOG
========================================== */

document
    .getElementById("singleLogForm")
    .addEventListener(
        "submit",
        async event => {

            event.preventDefault();


            const level =
                document.getElementById(
                    "singleLevel"
                ).value;


            const message =
                document.getElementById(
                    "singleMessage"
                ).value.trim();


            const result =
                document.getElementById(
                    "singleResult"
                );


            if (!message) {

                result.innerHTML =
                    `<span class="failure">
                        Message is required.
                    </span>`;

                return;
            }


            const button =
                event.target.querySelector(
                    "button"
                );


            button.disabled = true;

            button.textContent =
                "ADDING...";


            try {

                const response =
                    await fetch(
                        `${API}/logs`,
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify({
                                    level,
                                    message
                                })
                        }
                    );


                const data =
                    await response.json();


                if (!response.ok) {

                    throw new Error(
                        data.error ||
                        "Failed to add log"
                    );
                }


                result.innerHTML =
                    `<span class="success">
                        ✓ Log added successfully.
                    </span>`;


                document.getElementById(
                    "singleMessage"
                ).value = "";


                showToast(
                    "Log event added"
                );


                await refreshDashboard();


            } catch (error) {

                console.error(error);

                result.innerHTML =
                    `<span class="failure">
                        ⚠ ${escapeHTML(
                            error.message
                        )}
                    </span>`;

            } finally {

                button.disabled = false;

                button.textContent =
                    "+ ADD EVENT";
            }
        }
    );


/* ==========================================
   BULK LOG INPUT
========================================== */

const bulkInput =
    document.getElementById(
        "bulkLogInput"
    );

const processBulkButton =
    document.getElementById(
        "processBulkLogs"
    );

const clearBulkButton =
    document.getElementById(
        "clearBulkLogs"
    );

const bulkResult =
    document.getElementById(
        "bulkResult"
    );


processBulkButton.addEventListener(
    "click",
    async () => {

        const bulkText =
            bulkInput.value.trim();


        if (!bulkText) {

            bulkResult.innerHTML =
                `<span class="failure">
                    ⚠ Please enter some logs.
                </span>`;

            return;
        }


        processBulkButton.disabled =
            true;

        processBulkButton.textContent =
            "PROCESSING...";


        bulkResult.innerHTML =
            `<span class="processing">
                ⟳ Processing logs...
            </span>`;


        try {

            const response =
                await fetch(
                    `${API}/logs/bulk`,
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify({
                                logs: bulkText
                            })
                    }
                );


            const data =
                await response.json();


            if (!response.ok) {

                throw new Error(
                    data.error ||
                    "Failed to process logs"
                );
            }


            bulkResult.innerHTML = `

                <div class="success">

                    ✓ Bulk ingestion completed

                    <br>

                    <small>
                        Total:
                        ${data.totalLines}
                        &nbsp; | &nbsp;
                        Processed:
                        ${data.processed}
                        &nbsp; | &nbsp;
                        Failed:
                        ${data.failed}
                    </small>

                </div>
            `;


            bulkInput.value = "";


            showToast(
                `${data.processed} logs processed`
            );


            await refreshDashboard();


        } catch (error) {

            console.error(error);

            bulkResult.innerHTML =
                `<span class="failure">
                    ⚠ ${escapeHTML(
                        error.message
                    )}
                </span>`;

        } finally {

            processBulkButton.disabled =
                false;

            processBulkButton.textContent =
                "⚡ PROCESS LOGS";
        }

    }
);


/* ==========================================
   CLEAR BULK INPUT
========================================== */

clearBulkButton.addEventListener(
    "click",
    () => {

        bulkInput.value = "";

        bulkResult.innerHTML = "";

    }
);


/* ==========================================
   SEARCH / FILTER
========================================== */

document
    .getElementById("searchInput")
    .addEventListener(
        "input",
        renderLogs
    );


document
    .getElementById("levelFilter")
    .addEventListener(
        "change",
        renderLogs
    );


/* ==========================================
   AUTO REFRESH
========================================== */

refreshDashboard();


setInterval(
    refreshDashboard,
    3000
);
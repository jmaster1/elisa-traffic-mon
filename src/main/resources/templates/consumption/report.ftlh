<@layout>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <section class="d-grid gap-3">
        <#if monthlyConsumptionProgress?? && monthlyConsumptionProgress?size gt 0>
            <section class="d-grid gap-2">
                <#list monthlyConsumptionProgress as item>
                    <div class="progress position-relative"
                         style="height: 1.5rem;"
                         role="progressbar"
                         aria-label="${item.label}"
                         aria-valuenow="${item.cappedPercent}"
                         aria-valuemin="0"
                         aria-valuemax="100">

                        <div class="progress-bar"
                             style="width: ${item.cappedPercent}%; background-color: ${item.color};">
                        </div>

                        <div class="progress-content">
                            <span class="progress-label">${item.label}</span>
                            <span class="progress-usage">${item.usedGbText} / ${item.quotaGb} GB</span>
                            <span class="progress-percent">${item.percent}%</span>
                        </div>

                    </div>
                </#list>
            </section>
        </#if>

        <@pageFilter formState=reportFilter />

        <div style="width: 100%; height: 620px">
            <canvas id="consumptionChart"></canvas>
        </div>
    </section>

    <script>
        const config = ${chartJson?no_esc};
        const dateFormatter = new Intl.DateTimeFormat(undefined, {
            timeZone: '${reportZoneId?js_string}',
            month: 'short',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });

        config.options = {
            ...config.options,
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'nearest',
                intersect: false
            },
            plugins: {
                ...config.options?.plugins,
                legend: {
                    position: 'bottom'
                },
                tooltip: {
                    callbacks: {
                        title: (items) => items.length ? dateFormatter.format(new Date(items[0].parsed.x)) : '',
                        label: (item) => item.dataset.label + ': ' + item.parsed.y + ' GB'
                    }
                }
            },
            scales: {
                x: {
                    type: 'linear',
                    ticks: {
                        callback: (value) => dateFormatter.format(new Date(value))
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'GB'
                    },
                    beginAtZero: true
                }
            }
        };

        new Chart(
            document.getElementById('consumptionChart'),
            config
        );
    </script>
</@layout>

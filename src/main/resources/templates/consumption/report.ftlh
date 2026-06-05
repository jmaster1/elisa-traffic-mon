<@layout>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <section class="d-grid gap-3">
        <@pageFilter formState=reportFilter />

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
                             style="width: ${item.cappedPercent}%; background-color: ${item.color};"></div>
                        <div class="position-absolute top-0 bottom-0 start-0 end-0 d-grid align-items-center px-3"
                             style="grid-template-columns: minmax(8rem, 1fr) minmax(10rem, 1fr) 6rem; column-gap: 1rem;">
                            <span>${item.label}</span>
                            <span>${item.usedGbText} / ${item.quotaGb} GB</span>
                            <span class="text-end">${item.percent}%</span>
                        </div>
                    </div>
                </#list>
            </section>
        </#if>

        <div style="width: 100%; height: 620px">
            <canvas id="consumptionChart"></canvas>
        </div>
    </section>

    <script>
        const config = ${chartJson?no_esc};
        const dateFormatter = new Intl.DateTimeFormat(undefined, {
            timeZone: 'Europe/Tallinn',
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

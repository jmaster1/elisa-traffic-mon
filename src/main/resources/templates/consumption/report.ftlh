<@layout>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <section class="d-grid gap-3">
        <div>
            <h1 class="h4 mb-1">Consumption report</h1>
            <div class="text-secondary">Used GB by phone number</div>
        </div>

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

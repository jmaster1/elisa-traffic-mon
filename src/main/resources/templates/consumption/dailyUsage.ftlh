<@layout>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <section class="d-grid gap-3">
        <@pageFilter formState=reportFilter />

        <div style="width: 100%; height: 620px">
            <canvas id="dailyUsageChart"></canvas>
        </div>
    </section>

    <script>
        const config = ${chartJson?no_esc};

        config.options = {
            ...config.options,
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false
            },
            plugins: {
                ...config.options?.plugins,
                legend: {
                    position: 'bottom'
                },
                tooltip: {
                    callbacks: {
                        label: (item) => item.dataset.label + ': ' + item.parsed.y + ' GB'
                    }
                }
            },
            scales: {
                x: {
                    stacked: false
                },
                y: {
                    title: {
                        display: true,
                        text: 'GB per day'
                    },
                    beginAtZero: true
                }
            }
        };

        new Chart(
            document.getElementById('dailyUsageChart'),
            config
        );
    </script>
</@layout>

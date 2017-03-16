module.exports = {
    entry: './js/app.js',
    output: {
        filename: 'assets/dig-libs.js'
    },
    module: {
        rules: [
            {test: /\.css$/,
             use: ['style-loader', 'css-loader']}
        ]
    }
}
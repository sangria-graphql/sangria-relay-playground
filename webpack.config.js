module.exports = {
    entry: "./app/javascripts/js/app.js",
    output: {
        path: __dirname + "/public/javascripts",
        filename: "bundle.js"
    },
    module: {
        loaders: [
            {
                test: /\.js$/, exclude: /node_modules/, loader: 'babel-loader',
                query: {
                    presets: ['react', 'es2015', 'stage-0'],
                    plugins: ['./babelRelayPlugin']
                }
            }
        ]
    }
};
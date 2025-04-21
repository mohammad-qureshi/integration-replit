from flask import Flask, render_template, redirect, jsonify
import os

app = Flask(__name__)
app.secret_key = os.environ.get("SESSION_SECRET", "dev-secret-key")

@app.route('/')
def index():
    return redirect('/api/docs')

@app.route('/api/docs')
def docs():
    return """
    <html>
    <head>
        <title>Git Integration API - Flask Interface</title>
        <link href="https://cdn.replit.com/agent/bootstrap-agent-dark-theme.min.css" rel="stylesheet">
    </head>
    <body class="p-4">
        <div class="container">
            <h1>Git Integration API - Flask Interface</h1>
            <p>This is a simple Flask interface to the Git Integration API. The main application is built with Spring Boot.</p>
            <p>To access the Spring Boot application, please start the spring-app workflow.</p>
            <div class="alert alert-info">
                <h4>API Status</h4>
                <p>Spring Boot Application: Not Running</p>
                <p>Flask Application: Running</p>
            </div>
        </div>
    </body>
    </html>
    """

@app.route('/api/health')
def health():
    return jsonify({"status": "UP", "service": "Flask Interface"})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
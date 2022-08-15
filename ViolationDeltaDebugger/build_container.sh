docker build . -t delta_debugger_test

docker run -p 8000:5005 delta_debugger_test

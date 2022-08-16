out=$1
error='java.lang.ArithmeticException: / by zero'
if [[ "$out" == *"$error"* ]]; then
  exit 0
fi
exit 1

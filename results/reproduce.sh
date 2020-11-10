echo "Building the project..."
mvn clean compile assembly:single

for ((problemNum = 1; problemNum <= 12; problemNum++)); do
  for ((testNum = 1; testNum <= 10; testNum++)); do
    echo "Test run $testNum executing on src/main/resources/problems/exam_comp_set$problemNum.exam..."
    java -jar target/cos_790_assignment_1-1.0-SNAPSHOT-jar-with-dependencies.jar -p src/main/resources/problems/exam_comp_set$problemNum.exam -r $problemNum -g src/main/resources/genetic.parameters -o results/exam_comp_set$problemNum/run_$num.result
  done
done

echo "Success! All tests have been completed successfully. See the 'results' folder for all output files."
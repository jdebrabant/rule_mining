#Runs with fixed input.
#java -cp dist/lib/dom4j.jar -jar dist/galaxy.jar -i ./TwoSystems.xml -o result.xml

#Runs with random input. Arguments:
# -p: number of planets
# -r: num of rockets
# -t: timesteps
#Positions of planets and rockets will be calculated randomly
java -cp dist/lib/dom4j.jar -jar dist/galaxy.jar -p 100 -r 50 -t 1000 -o result.xml

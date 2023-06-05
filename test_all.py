import subprocess
from time import sleep

client = 'java -jar ./out/artifacts/phrases_client/phrases_client.jar'
server = 'java -jar ./out/artifacts/phrases_server/phrases_server.jar'

server_p = subprocess.Popen(server)
sleep(0.5)

# start all programs
processes = [subprocess.Popen(program) for program in [client, client]]
processes.append(server_p)
# wait
for process in processes:
    process.wait()
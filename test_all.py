import subprocess
from platform import system
from time import sleep

client = 'java -jar ./out/artifacts/phrases_client/phrases_client.jar'.split()
server = 'java -jar ./out/artifacts/phrases_server/phrases_server.jar'.split()

server_p = subprocess.Popen(server)
sleep(0.5)

# start all programs
processes = [subprocess.Popen(program) for program in [client, client]]
processes.append(server_p)
# wait
for process in processes:
    process.wait()

# os = system()
# if os == "Windows":
#     files = subprocess.check_output(['powershell'])
#     print(files)
#
#     for l in files:
#         print(l)

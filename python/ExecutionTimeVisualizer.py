import socket
import matplotlib.pyplot as plt
from collections import defaultdict


def receive(socket_obj):
    data: bytes = socket_obj.recv(1024)
    return data.decode('utf-8')


def send(message, socket_obj):
    encoded_message = message.encode('utf-8')
    message_length = len(encoded_message)
    socket_obj.send(message_length.to_bytes(2, byteorder='big'))
    socket_obj.send(encoded_message)


def get_time_table():
    time_table = defaultdict(lambda: defaultdict(float))
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as serverSocket:
        serverSocket.connect(('localhost', 9090))

        exit_hash = receive(serverSocket)
        send("", serverSocket)
        print(exit_hash)

        files_amount_list = list()

        while (received_message := receive(serverSocket)) != exit_hash:
            files_amount_list.append(received_message)
            send("", serverSocket)
        send("", serverSocket)
        print(files_amount_list)

        thread_range = receive(serverSocket)
        send("", serverSocket)
        print(thread_range)

        files_range = len(files_amount_list)

        for _ in range(int(thread_range)):
            thread_amount = receive(serverSocket)
            send("", serverSocket)

            if thread_amount == exit_hash:
                break

            print('Thread amount:', thread_amount)

            for files_amount in range(int(files_range)):
                exec_nano_time = receive(serverSocket)
                send("", serverSocket)
                print(f'{files_amount}: exec time:', int(exec_nano_time) / 1000000.0)

                time_table[files_amount_list[files_amount]][thread_amount] = int(exec_nano_time) / 1000000.0
            print()

        print('Done job')
        print(time_table)
        return time_table


def main():
    time_table = get_time_table()

    files_range = time_table.keys()
    threads_range = list(time_table.values())[0].keys()

    print(files_range)
    print(threads_range)

    for files_amount in files_range:
        xs, ys = zip(*time_table[files_amount].items())
        plt.plot(xs, ys, color='#01bc17')
        plt.title(f'Execution time for {files_amount} files')
        plt.xlabel(f'Threads, amount')
        plt.ylabel(f'Execution time, ms')
        plt.savefig(f'../assets/screenshots/exec-time-{files_amount}', dpi=800, bbox_inches='tight')
        plt.close()

    for files_amount in files_range:
        xs, ys = zip(*time_table[files_amount].items())
        plt.plot(xs, ys)
        plt.legend(files_range, title='Amount of files')
        plt.title(f'General execution time')
        plt.xlabel(f'Threads, amount')
        plt.ylabel(f'Execution time, ms')
        plt.savefig(f'../assets/screenshots/exec-time-total', dpi=800, bbox_inches='tight')


if __name__ == '__main__':
    main()

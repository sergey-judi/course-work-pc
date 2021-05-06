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
        
        # receive secret hash to send it to the server later 
        # to let him know that client finished execution [of some block]
        exit_hash = receive(serverSocket)
        # let server know that client as received his message
        send("", serverSocket)
        print(exit_hash)

        files_amount_list = list()

        # receive files amounts until the server sends the exit hash
        while (received_message := receive(serverSocket)) != exit_hash:
            files_amount_list.append(received_message)
            send("", serverSocket)
        send("", serverSocket)
        print(files_amount_list)

        # receive the right board for thread amount interval
        thread_range = receive(serverSocket)
        send("", serverSocket)
        print(thread_range)

        files_range = len(files_amount_list)

        for _ in range(int(thread_range)):
            # receive current amount of threads used to create an inverted index
            thread_amount = receive(serverSocket)
            send("", serverSocket)

            # for the situation if the server decides to finish its execution
            if thread_amount == exit_hash:
                break

            print('Thread amount:', thread_amount)

            # receive execution time for each files amount
            for files_amount in range(int(files_range)):
                exec_nano_time = receive(serverSocket)
                send("", serverSocket)
                print(f'{files_amount}: exec time:', int(exec_nano_time) / 1000000.0)
                # put execution time in milliseconds to a dictionary
                time_table[files_amount_list[files_amount]][thread_amount] = int(exec_nano_time) / 1000000.0
            print()

        print('Done job')
        print(time_table)
        return time_table


def main():
    # obtain a dictionary of execution time 
    # for definite threads amount used to create an inverted index 
    # for the files amount given
    # execution_time = time_table[files_amount][threads_amount]
    time_table = get_time_table()

    files_range = time_table.keys()
    threads_range = list(time_table.values())[0].keys()

    print(files_range)
    print(threads_range)

    # plot execution time curve for each amount of files on a separate plot
    for files_amount in files_range:
        xs, ys = zip(*time_table[files_amount].items())
        plt.plot(xs, ys, color='#01bc17')
        plt.title(f'Execution time for {files_amount} files')
        plt.xlabel(f'Threads, amount')
        plt.ylabel(f'Execution time, ms')
        plt.savefig(f'/assets/screenshots/exec-time-{files_amount}', dpi=800, bbox_inches='tight')
        plt.close()

    # plot execution time curve for all amount of files on one plot
    for files_amount in files_range:
        xs, ys = zip(*time_table[files_amount].items())
        plt.plot(xs, ys)
        plt.legend(files_range, title='Amount of files')
        plt.title(f'General execution time')
        plt.xlabel(f'Threads, amount')
        plt.ylabel(f'Execution time, ms')
        plt.savefig(f'/assets/screenshots/exec-time-total', dpi=800, bbox_inches='tight')


if __name__ == '__main__':
    main()

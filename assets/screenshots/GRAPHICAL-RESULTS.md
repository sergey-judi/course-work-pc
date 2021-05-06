# Graphical results
Execution time plots for inverted index built on different amount of files running on different amount of threads
## Table of contents
- [Index built for 2000 files](#Index-built-for-2000-files)
- [Index built for 5000 files](#Index-built-for-5000-files)
- [Index built for 20000 files](#Index-built-for-20000-files)
- [Index built for 50000 files](#Index-built-for-50000-files)
- [Index built for 100000 files](#Index-built-for-100000-files)
- [General](#General)

### Index built for 2000 files
- Using __CopyOnWriteArrayList__ data structure as thread-safe collection for inverted index
    ![2000-1](./concurrent-list-exec-time-2000.png)
- Using __ConcurrentLinkedQueue__ data structure as thread-safe collection for inverted index
    ![2000-2](./concurrent-queue-exec-time-2000.png)

### Index built for 5000 files
- Using __CopyOnWriteArrayList__ data structure as thread-safe collection for inverted index
    ![5000-1](./concurrent-list-exec-time-5000.png)
- Using __ConcurrentLinkedQueue__ data structure as thread-safe collection for inverted index
    ![5000-2](./concurrent-queue-exec-time-5000.png)

### Index built for 20000 files
- Using __CopyOnWriteArrayList__ data structure as thread-safe collection for inverted index
    ![20000-1](./concurrent-list-exec-time-20000.png)
- Using __ConcurrentLinkedQueue__ data structure as thread-safe collection for inverted index
    ![20000-2](./concurrent-queue-exec-time-20000.png)

### Index built for 50000 files
- Using __CopyOnWriteArrayList__ data structure as thread-safe collection for inverted index
    ![50000-1](./concurrent-list-exec-time-50000.png)
- Using __ConcurrentLinkedQueue__ data structure as thread-safe collection for inverted index
    ![50000-2](./concurrent-queue-exec-time-50000.png)

### Index built for 100000 files
- Using __CopyOnWriteArrayList__ data structure as thread-safe collection for inverted index
    ![100000-1](./concurrent-list-exec-time-100000.png)
- Using __ConcurrentLinkedQueue__ data structure as thread-safe collection for inverted index
    ![100000-2](./concurrent-queue-exec-time-100000.png)

### General
- Using __CopyOnWriteArrayList__ data structure as thread-safe collection for inverted index
    ![total-1](./concurrent-list-exec-time-total.png)
- Using __ConcurrentLinkedQueue__ data structure as thread-safe collection for inverted index
    ![total-2](./concurrent-queue-exec-time-total.png)

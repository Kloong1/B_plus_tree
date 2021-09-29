# B+ tree

Disk access 개념은 제외하고 indexing 개념만 구현한 B+ tree입니다.

index tree와 key-value를 single file에서 read/write 하는 방식으로 단순화해서 구현했습니다.

## How to execute

    java -jar bptree.jar [-cidsr] [index file path] [insert/delete file path or key to search]



### B+ tree creation

    java -jar bptree.jar -c index.dat 100
  
index.dat 파일에 100 degree의 B+ tree가 기록됩니다.


### B+ tree insertion

    java -jar bptree.jar -i index.dat insert.csv

index.dat 파일에 기록된 B+ tree를 읽어들입니다. 그 후에 insert.csv 파일의 key-value pair를 B+ tree에 삽입합니다.


### B+ tree deletion

    java -jar bptree.jar -d index.dat delete.csv
    
index.dat 파일에 기록된 B+ tree를 읽어들입니다. 그 후에 delete.csv 파일의 key를 B+ tree에서 삭제합니다. key에 해당하는 value도 삭제합니다.


### B+ tree single key search

    java -jar bptree.jar -s index.dat 3
    
index.dat 파일에 기록된 B+ tree를 읽어들입니다. 그 후에 key 3을 찾아서 key가 가리키는 value를 출력합니다.


### B+ tree ranged search

    java -jar bptree.jar -r index.dat 5 100
    
index.dat 파일에 기록된 B+ tree를 읽어들입니다. 그 후에 5 이상 100 이하의 key-value pair를 출력합니다.


## Insert/delete 파일

Key와 value는 int입니다.

### Insert file

    <key>,<value>\n
    <key>,<value>\n
    ...

###delete file

    <key>\n
    <key>\n
    ...
    

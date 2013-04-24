all: c 

init-c: init.c
	gcc -o init-c -Wall -march=native -W -Wextra -Werror -std=gnu99 -Wl,-O3 -Wl,--as-needed -static init.c

c: init-c
	cp init-c init

bb: init.bb
	cp init.bb init
	chmod +x init

clean:
	rm -f init

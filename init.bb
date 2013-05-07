#!/bin/busybox sh

rescue_shell() {
        echo "Error - rescue shell"
                busybox --install -s
        exec /bin/sh
}

mount -t proc none /proc
mount -t sysfs none /sys
mount -t devtmpfs none /dev


openssl aes-256-cbc -d -in /root_key.enc | \
cryptsetup --allow-discards --key-file=- luksOpen /dev/sda4 root || rescue_shell

mount -o ro /dev/mapper/root /mnt/root || rescue_shell

umount /proc
umount /sys
umount /dev

exec switch_root /mnt/root /sbin/init

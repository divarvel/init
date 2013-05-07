#!/bin/busybox sh

busybox --install -s

rescue_shell() {
    exec 1<>/dev/console 2<>/dev/console
    echo "Something went wrong. Dropping you to a shell."
    exec sh
}

rescue_shell

#mount -t proc none /proc
#mount -t sysfs none /sys
#mount -t devtmpfs none /dev
#
#
#openssl aes-256-cbc -d -in /root_key.enc | \
#cryptsetup --allow-discards --key-file=- luksOpen /dev/sda4 root || rescue_shell
#
#mount -o ro /dev/mapper/root /mnt/root || rescue_shell
#
#umount /proc
#umount /sys
#umount /dev
#
#exec switch_root /mnt/root /bin/systemd

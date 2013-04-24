#!/bin/busybox-static sh

mount -t proc none /proc
mount -t sysfs none /sys
mount -t devtmpfs none /dev

openssl aes-256-cbc -d -in /root_key.inc | cryptsetup --allow-discards --key-file=- luksOpen /dev/sda4 root

umount /proc
umount /sys
umount /dev

mount /dev/mapper/root /root

exec switch_root /root /usr/lib64/systemd/systemd

#! /bin/busybox sh

/bin/busybox --install -s
rescue_shell()
{
	exec 1<>/dev/console 2<>/dev/console
	echo "Something went wrong. Dropping you to a shell."
    exec sh
}

unlock_root()
{
	/usr/bin/openssl aes-256-cbc -d -in /root_key.enc | \
	/sbin/cryptsetup --allow-discards --key-file=- luksOpen /dev/sda4 root || unlock_root
}

unlock_data()
{
	/usr/bin/openssl aes-256-cbc -d -in /data_key.enc | \
	/sbin/cryptsetup --allow-discards --key-file=- luksOpen /dev/sda5 data || unlock_data
}


echo "Initramfs booting..."

mount -t proc none /proc || rescue_shell
mount -t sysfs none /sys || rescue_shell
mount -t devtmpfs none /dev || rescue_shell

echo 0 > /proc/sys/kernel/printk

unlock_root
unlock_data

echo "Mounting root device: /dev/mapper/root..."
mount -n -o ro /dev/mapper/root /mnt/root || rescue_shell

echo "Unmounting /sys and /proc ..."
umount -f /sys /proc || rescue_shell

echo "### resuming normal boot ###"
exec switch_root /mnt/root /sbin/init

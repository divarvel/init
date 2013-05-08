#! /bin/busybox sh

/bin/busybox --install -s
rescue_shell()
{
	exec 1<>/dev/console 2<>/dev/console
	echo "Something went wrong. Dropping you to a shell."
    exec sh
}

unlock()
{
	/usr/bin/openssl aes-256-cbc -d -in /root_key.enc | \
	/sbin/cryptsetup --allow-discards --key-file=- luksOpen /dev/sda4 root || unlock 
}


echo "Initramfs booting..."

mount -t proc none /proc || rescue_shell
mount -t sysfs none /sys || rescue_shell

echo "Scanning for device nodes..."
echo /sbin/mdev > /proc/sys/kernel/hotplug
mdev -s || rescue_shell

unlock

echo "Mounting root device: /dev/mapper/root..."
mount -n -o ro /dev/mapper/root /mnt/root || rescue_shell

echo "Unmounting /sys and /proc ..."
umount -f /sys /proc || rescue_shell

echo "### resuming normal boot ###"
exec switch_root /mnt/root /sbin/init

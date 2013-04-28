#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include <sys/mount.h>
#include <sys/wait.h>


#define INIT_PATH "/usr/lib64/systemd/systemd"
#define ROOT_PATH "/dev/mapper/root"
#define ROOT_FS_TYPE "ext4"

void perform_mounts()
{
	mount("none", "/proc", "proc", 0, NULL);
	mount("none", "/sys", "sysfs", 0, NULL);
	mount("none", "/dev", "devtmpfs", 0, NULL);
}

void perform_umounts()
{
	umount("/proc");
	rmdir("/proc");

	umount("/sys");
	rmdir("/sys");

	umount("/dev");
	rmdir("/dev");
}

void unlock_root()
{
	int	fd[2];
	int 	status;
	pid_t	pid;
	pid_t	cid;

	char *openssl_args[] = {"openssl", "aes-256-cbc", "-d", "-in", "/root_key.enc", NULL};
	char *cryptsetup_args[] = {"cryptsetup", "--allow-discards", "--key-file=-", "luksOpen", "/dev/sda4", "root", NULL};

	pid = fork();
	
	if(pid == 0)
	{
		pipe(fd);

		cid = fork();

		if(cid > 0)
		{
			close(fd[0]); // Close pipe output
			close(STDOUT_FILENO); // Close stdout
			dup(fd[1]); // make the pipe input stdout
			close(fd[1]);
			execv("/usr/bin/openssl", openssl_args);
			
			exit(0);
		}
		else
		{
			close(fd[1]); // Close pipe input
			close(STDIN_FILENO); // Close stdout
			dup(fd[0]); // make the pipe output stdin
			close(fd[0]);
			execv("/usr/bin/cryptsetup", cryptsetup_args);
			
			exit(0);
		}
		
	} else {
		waitpid(-1, &status, 0);
	}
}

void mount_root()
{
	mount(ROOT_PATH, "/mnt/root", ROOT_FS_TYPE, MS_RDONLY, NULL);
}

int main()
{
	perform_mounts();	
	unlock_root();
	mount_root();
	perform_umounts();	

	if(chdir("/root") != 0) return -1;

	mount(".", "/", NULL, MS_MOVE, NULL);

	if(chroot(".") != 0) return -1;
	if(chdir("/") != 0) return -1;

	return execl(INIT_PATH, INIT_PATH, NULL);
}


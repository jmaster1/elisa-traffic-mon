---------------------------------------------------------
etm.jmaster.online
---------------------------------------------------------
sudo bash -c "$(curl -fsSL https://raw.githubusercontent.com/jmaster1/bat-setup/main/etm/setup.sh)"
sudo bash -c "$(curl -fsSL https://raw.githubusercontent.com/jmaster1/bat-setup/main/etm/deploy.sh)"

sudo nano /etc/systemd/system/etm.service
sudo systemctl stop etm

package android.bignerdranch.com.qsync;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class ViewKioskViewModel extends ViewModel {
    private final MutableLiveData<Kiosk> kiosk = new MutableLiveData<>();
    private final MutableLiveData<List<User>> hosts = new MutableLiveData<>();
    private final MutableLiveData<List<Session>> clients = new MutableLiveData<>();

    public void setKiosk(Kiosk kiosk) {
        this.kiosk.setValue(kiosk);
    }

    public LiveData<Kiosk> getKiosk() {
        return kiosk;
    }

    public void setHosts(List<User> hosts) {
        this.hosts.setValue(hosts);
    }

    public LiveData<List<User>> getHosts() {
        return hosts;
    }

    public void setClients(List<Session> clients) {
        this.clients.setValue(clients);
    }

    public LiveData<List<Session>> getClients() {
        return clients;
    }
}

package br.com.payflow.merchant.features.provision;

import br.com.payflow.merchant.domain.Merchant;

// Output port used only by the provisioning slice.
public interface MerchantProvisioning {

    // Atomically inserts the candidate or returns the existing merchant without changing it.
    Merchant insertIfAbsent(Merchant candidate);
}

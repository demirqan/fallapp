    package com.falapp.falciabla.utils;



    import android.app.Activity;
    import android.content.Context;
    import android.content.SharedPreferences;
    import android.util.Log;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;

    import com.android.billingclient.api.AcknowledgePurchaseParams;
    import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
    import com.android.billingclient.api.BillingClient;
    import com.android.billingclient.api.BillingClientStateListener;
    import com.android.billingclient.api.BillingFlowParams;
    import com.android.billingclient.api.BillingResult;
    import com.android.billingclient.api.ConsumeParams;
    import com.android.billingclient.api.ConsumeResponseListener;
    import com.android.billingclient.api.Purchase;
    import com.android.billingclient.api.PurchasesResponseListener;
    import com.android.billingclient.api.PurchasesUpdatedListener;
    import com.android.billingclient.api.QueryProductDetailsParams;
    import com.android.billingclient.api.QueryPurchasesParams;
    import com.android.billingclient.api.ProductDetails;
    import com.android.billingclient.api.ProductDetailsResponseListener;
    import com.falapp.falciabla.models.User;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public class BillingManager implements PurchasesUpdatedListener {

        private static final String TAG = "BillingManager";


        public static final String PRODUCT_COINS_50 = "coins_50_new";
        public static final String PRODUCT_COINS_120 = "coins_120_new";
        public static final String PRODUCT_COINS_300 = "coins_300_new";
        public static final String SUBSCRIPTION_PREMIUM_MONTHLY = "premium_monthly";
        public static final String SUBSCRIPTION_PREMIUM_YEARLY = "premium_yearly";
        public static final String PRODUCT_PREMIUM_LIFETIME = "premium_lifetime";


        private BillingClient billingClient;
        private Context context;
        private Activity activity;
        private DatabaseHelper dbHelper;
        private User currentUser;

        private static final String PREFS_NAME = "app_prefs";
        private static final String PREMIUM_BONUS_GIVEN = "premium_bonus_given";


        private BillingCallback billingCallback;
        private Map<String, ProductDetails> productDetailsMap;

        public interface BillingCallback {
            void onBillingSetupFinished(boolean isSuccess);
            void onPurchaseSuccess(String productId, int coins, boolean isPremium);
            void onPurchaseFailed(int errorCode, String errorMessage);
        }

        public BillingManager(Context context, Activity activity, DatabaseHelper dbHelper, User currentUser, BillingCallback callback) {
            this.context = context;
            this.activity = activity;
            this.dbHelper = dbHelper;
            this.currentUser = currentUser;
            this.billingCallback = callback;
            this.productDetailsMap = new HashMap<>();
            setupBillingClient();
        }


        private void setupBillingClient() {
            billingClient = BillingClient.newBuilder(context)
                    .setListener(this)
                    .enablePendingPurchases()
                    .build();

            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                        Log.d(TAG, "BillingClient bağlantısı başarılı.");


                        queryProductDetails();


                        queryPurchases();

                        if (billingCallback != null) {
                            billingCallback.onBillingSetupFinished(true);
                        }
                    } else {

                        Log.e(TAG, "BillingClient bağlantısı başarısız: " + billingResult.getDebugMessage());
                        if (billingCallback != null) {
                            billingCallback.onBillingSetupFinished(false);
                        }
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {

                    Log.d(TAG, "BillingClient bağlantısı kesildi. Tekrar bağlanılacak.");
                    billingClient.startConnection(this); // yeniden bağlanma zaten içkin mekanizmadır
                }
            });
        }

        /**
         * Ürün detaylarını sorgula
         */
        private void queryProductDetails() {
            // 🟢 INAPP (tek seferlik ürünler)
            List<QueryProductDetailsParams.Product> inappList = Arrays.asList(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_COINS_50)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_COINS_120)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_COINS_300)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_PREMIUM_LIFETIME)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
            );

            QueryProductDetailsParams inappParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(inappList)
                    .build();

            billingClient.queryProductDetailsAsync(inappParams, (billingResult, productDetailsList) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (ProductDetails details : productDetailsList) {
                        Log.d(TAG, "INAPP Ürün: " + details.getProductId());
                        productDetailsMap.put(details.getProductId(), details);
                    }
                } else {
                    Log.e(TAG, "INAPP ürünleri alınamadı: " + billingResult.getDebugMessage());
                }
            });

            // 🟣 SUBS (abonelik ürünleri)
            List<QueryProductDetailsParams.Product> subsList = Arrays.asList(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(SUBSCRIPTION_PREMIUM_MONTHLY)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(SUBSCRIPTION_PREMIUM_YEARLY)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
            );

            QueryProductDetailsParams subsParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(subsList)
                    .build();

            billingClient.queryProductDetailsAsync(subsParams, (billingResult, productDetailsList) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (ProductDetails details : productDetailsList) {
                        Log.d(TAG, "SUBS Ürün: " + details.getProductId());
                        productDetailsMap.put(details.getProductId(), details);
                    }
                } else {
                    Log.e(TAG, "SUBS ürünleri alınamadı: " + billingResult.getDebugMessage());
                }
            });
        }

        /**
         * Tamamlanmamış satın alımları sorgula
         */

        public void queryPurchases() {
            // 1. Abonelikleri kontrol et
            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                    (billingResult, purchases) -> {
                        boolean isCurrentlyPremium = false;

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (Purchase purchase : purchases) {
                                for (String productId : purchase.getProducts()) {
                                    if ((productId.equals(SUBSCRIPTION_PREMIUM_MONTHLY) ||
                                            productId.equals(SUBSCRIPTION_PREMIUM_YEARLY)) &&
                                            purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED &&
                                            purchase.isAcknowledged()) {
                                        isCurrentlyPremium = true;
                                        break;
                                    }
                                }
                            }

                            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            prefs.edit().putBoolean("is_premium", isCurrentlyPremium).apply();

                            if (currentUser != null) {
                                currentUser.setPremium(isCurrentlyPremium);
                                dbHelper.updateUserPremium(currentUser.getId(), isCurrentlyPremium);
                            }

                            Log.d(TAG, "🔄 Abonelikten premium durumu güncellendi: " + isCurrentlyPremium);
                        } else {
                            Log.e(TAG, "SUBS sorgusu hatalı: " + billingResult.getDebugMessage());
                        }
                    }
            );

            // 2. Tek seferlik (INAPP) ürünleri kontrol et ve tüket
            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    (billingResult, purchases) -> {
                        processPurchases(purchases); // EKLE
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "INAPP ürün sorgusu sonucu: " + purchases.size());

                            for (Purchase p : purchases) {
                                if (p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    for (String productId : p.getProducts()) {
                                        Log.d(TAG, "🎯 INAPP ürün var: " + productId);

                                        // Bu ürünler tüketilebilir
                                        if (productId.equals(PRODUCT_PREMIUM_LIFETIME) ||
                                                productId.equals("coins_50_new") ||
                                                productId.equals("coins_120_new") ||
                                                productId.equals("coins_300_new")) {

                                            // Zaten acknowledged olanı tüket

                                        }
                                    }
                                }
                            }
                        } else {
                            Log.e(TAG, "INAPP sorgusu hatalı: " + billingResult.getDebugMessage());
                        }
                    }
            );
        }



        /**
         * Satın alımları işle
         */
        private void processPurchases(List<Purchase> purchases) {
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    Log.d(TAG, "Satın alım bulundu, işleniyor: " + purchase.getProducts());

                    for (String productId : purchase.getProducts()) {
                        if (productId.equals(PRODUCT_COINS_50) ||
                                productId.equals(PRODUCT_COINS_120) ||
                                productId.equals(PRODUCT_COINS_300) ||
                                productId.equals(PRODUCT_PREMIUM_LIFETIME)) {
                            // INAPP ürün -> direkt tüket
                            consumePurchase(purchase);
                        } else {
                            // SUBS ürün veya farklı -> handle et
                            if (!purchase.isAcknowledged()) {
                                handlePurchase(purchase);
                            }
                        }
                    }
                } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    Log.d(TAG, "Satın alım beklemede: " + purchase.getProducts());
                } else {
                    Log.w(TAG, "Satın alım geçerli değil: " + purchase.getProducts());
                }
            }
        }

        /**
         * Satın alımı başlat
         */
        public void startPurchaseFlow(String productId) {


            ProductDetails details = productDetailsMap.get(productId);
            if (details == null) {

                return;
            }

            ProductDetails productDetails = productDetailsMap.get(productId);

            if (productDetails == null) {

                for (String key : productDetailsMap.keySet()) {
                    Log.e(TAG, " - " + key);
                }
                return;
            }

            BillingFlowParams.Builder paramsBuilder = BillingFlowParams.newBuilder();

            try {
                if (productDetails.getProductType().equals(BillingClient.ProductType.INAPP)) {
                    Log.d(TAG, "🎯 Tek seferlik ürün satın alma hazırlanıyor: " + productId);

                    paramsBuilder.setProductDetailsParamsList(
                            Arrays.asList(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .build()
                            )
                    );

                } else if (productDetails.getProductType().equals(BillingClient.ProductType.SUBS)) {
                    Log.d(TAG, "🎯 Abonelik ürünü satın alma hazırlanıyor: " + productId);

                    String offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();

                    paramsBuilder.setProductDetailsParamsList(
                            Arrays.asList(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .setOfferToken(offerToken)
                                            .build()
                            )
                    );
                }

                BillingFlowParams billingFlowParams = paramsBuilder.build();

                BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);

                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    Log.e(TAG, "❌ Satın alma akışı başlatılamadı: " + billingResult.getDebugMessage());
                    if (billingCallback != null) {
                        billingCallback.onPurchaseFailed(billingResult.getResponseCode(), billingResult.getDebugMessage());
                    }
                } else {
                    Log.d(TAG, "✅ Satın alma akışı başlatıldı.");
                }

            } catch (Exception e) {
                Log.e(TAG, "🛑 Hata oluştu: " + e.getMessage(), e);
            }
        }

        /**
         * Satın alımları takip et
         */
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                // Satın alım başarılı
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Kullanıcı iptal etti
                Log.d(TAG, "Kullanıcı satın almayı iptal etti.");
                if (billingCallback != null) {
                    billingCallback.onPurchaseFailed(billingResult.getResponseCode(), "Satın alma iptal edildi");
                }
            } else {
                // Hata
                Log.e(TAG, "Satın alma hatası: " + billingResult.getDebugMessage());
                if (billingCallback != null) {
                    billingCallback.onPurchaseFailed(billingResult.getResponseCode(), billingResult.getDebugMessage());
                }
            }
        }

        /**
         * Satın alımı işle
         */
        private void handlePurchase(Purchase purchase) {
            String productId = purchase.getProducts().get(0);

            Log.d(TAG, "✅ handlePurchase çağrıldı: " + productId);

            boolean isConsumable = productId.equals(PRODUCT_COINS_50) ||
                    productId.equals(PRODUCT_COINS_120) ||
                    productId.equals(PRODUCT_COINS_300) ||
                    productId.equals(PRODUCT_PREMIUM_LIFETIME);

            // INAPP ürünler: direkt tüket
            if (isConsumable) {
                Log.d(TAG, "🪙 INAPP ürün tespit edildi, tüketilecek: " + productId);
                consumePurchase(purchase);  // bu, token'ı sıfırlar
                completePurchase(productId, purchase);  // kullanıcıya ürün ver
                return;
            }

            // SUBS ürünler için acknowledge
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "🔄 Abonelik onaylandı: " + productId);
                        completePurchase(productId, purchase);
                    } else {
                        Log.e(TAG, "Abonelik onaylanamadı: " + billingResult.getDebugMessage());
                    }
                });
            } else {
                completePurchase(productId, purchase); // zaten onaylıysa işleme devam
            }
        }

        private void processCompletePurchase(Purchase purchase) {
            List<String> products = purchase.getProducts();
            if (products == null || products.isEmpty()) {
                Log.e(TAG, "Satın alım ürün listesi boş!");


                return;
            }

            for (String productId : products) {
                Log.d(TAG, "✅ Satın alma ürünü işleniyor: " + productId);
                completePurchase(productId, purchase);
            }
        }

        /**
         * Satın alımı tamamla ve kullanıcıya ürünleri ver
         */
        private void completePurchase(String productId, Purchase purchase) {
            Log.d(TAG, "completePurchase çağrıldı. productId: " + productId);

            int coins = 0;
            boolean isPremium = false;

            // isPremium VE coins değerini doğru şekilde belirle
            switch (productId) {
                case PRODUCT_COINS_50:
                    coins = 50;
                    isPremium = false;
                    break;
                case PRODUCT_COINS_120:
                    coins = 120;
                    isPremium = false;
                    break;
                case PRODUCT_COINS_300:
                    coins = 300;
                    isPremium = false;
                    break;
                case PRODUCT_PREMIUM_LIFETIME:
                    isPremium = true;
                    coins = 0;
                    break;
                case SUBSCRIPTION_PREMIUM_MONTHLY:
                    isPremium = true;
                    coins = 0;
                    break;
                case SUBSCRIPTION_PREMIUM_YEARLY:
                    isPremium = true;
                    coins = 0;
                    break;
            }

            if (currentUser != null) {
                // Coin ekleme
                if (coins > 0) {
                    currentUser.addCoins(coins);
                    dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
                }

                // Premium aktif etme
                if (isPremium) {
                    currentUser.setPremium(true);
                    dbHelper.updateUserPremium(currentUser.getId(), true);

                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("is_premium", true).apply();
                }

                // Coin ürünü ise tüket
                if (productId.startsWith("coins_")) {
                    Log.d(TAG, "🪙 Coin ürünü, tüketim başlatılıyor: " + productId);

                }
            }

            if (billingCallback != null) {
                billingCallback.onPurchaseSuccess(productId, coins, isPremium);
            }
        }




        /**
         * Satın alımı tüket (bir sonraki satın alma için)
         */
        private void consumePurchase(Purchase purchase) {
            String token = purchase.getPurchaseToken();
            if (token == null || token.isEmpty()) {
                Log.e(TAG, "⚠️ Purchase token geçersiz!");
                return;
            }

            ConsumeParams consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(token)
                    .build();

            billingClient.consumeAsync(consumeParams, (billingResult, purchaseToken) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "🟢 Satın alım tüketildi: " + purchaseToken);
                } else {
                    Log.e(TAG, "🔴 Satın alım tüketilemedi: " + billingResult.getDebugMessage());
                }
            });
        }

        /**
         * Ürün fiyatını al
         */
        public String getProductPrice(String productId) {
            ProductDetails productDetails = productDetailsMap.get(productId);
            if (productDetails != null) {
                if (productDetails.getProductType().equals(BillingClient.ProductType.INAPP)) {
                    return productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
                } else if (productDetails.getProductType().equals(BillingClient.ProductType.SUBS)) {
                    // İlk abonelik fiyatlandırma fazını kullan
                    return productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases()
                            .getPricingPhaseList().get(0).getFormattedPrice();
                }
            }
            return "Fiyat yüklenemedi";
        }

        /**
         * Bağlantıyı sonlandır
         */
        public void endConnection() {
            if (billingClient != null) {
                billingClient.endConnection();
            }
        }



    }
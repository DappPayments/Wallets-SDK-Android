# Dapp Wallet SDK Android #

**Dapp Wallet SDK** es la forma de identificar y leer códigos **Dapp** en una aplicación Android. En consecuencia cuenta con las siguientes funcionalidades:

* Comprobar que sea un código QR Dapp válido.
* Comprobar que sea un código QR Codi válido.
* Identificar el tipo de código QR.
* Obtener la información de un código QR Dapp.
* Invocar el lector de códigos QR de **Dapp Wallet SDK**

Para disponer de estas funcionalidades primero necesitas configurar **Dapp Wallet SDK** en tu aplicación como sigue:

1. Ve a Android Studio - New Project - Minimun SDK.

2. Selecciona *API 16: Android 4.1* o superior, y crea el proyecto.

3. Una vez creado el proyecto, abre *your_app | build.gradle*.

4. Añade esto a */app/build.gradle* en el nivel de *módulo* antes de *dependencies*:

```java

repositories {
  jcenter()
}

```

5. Añade la dependencia de compilación con la última versión de **Dapp Wallet SDK** al archivo *build.gradle*:

```java

dependencies {
  implementation 'mx.dapp:wallet-sdk:1.2.1'
}

```

6. Compila el proyecto y ya puedes inicializar Dapp Wallet en tu aplicación.

### Iniciar **Dapp Wallet SDK**

1. Explicación de como obtener tu *dapp_api_key*.

2. Abre el archivo *strings.xml*. Por ejemplo: */app/src/main/res/values/strings.xml*.

3. Añade una nueva cadena con el nombre *dapp_api_key*, que contiene el valor del identificador de la aplicación de Dapp Wallet:

```xml

<string name="dapp_api_key">dapp-api-key</string>

```

4. Abre el archivo *AndroidManifest.xml*.

5. Añade los elementos *uses-permission* al manifiesto:

```xml

<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.CAMERA" />

```

6. Ahora puedes iniciar **Dapp Wallet SDK** desde el método *onCreate* de tu actividad, señalando el ambiente en que deseas que funcione la libreria:

```java

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DappWallet.init(this, getString(R.string.dapp_api_key), DappEnviroment.SANDBOX);
}

```

### Utilizar **Dapp Wallet SDK** desde tu aplicación Android.

Una vez completados los pasos de **Configuración** e **Inicio** su aplicación esta lista para integrar las fucionalidades **Dapp Wallet**.

### Comprobar que es un QR Dapp válido y obtener la información del mismo

1. Invoque las funcionalidades de validar y posteriormente la de leer en el cuerpo de la actividad donde se inicio **Dapp Wallet SDK**:

```java
                 
try {
    String qr = "https://dapp.mx/c/oW9BYXqJ";
    if (DappWallet.isValidDappQR(qr)){
        DappWallet.readDappQR(qr, new DappWalletCallback() {
            @Override
            public void onSuccess(DappWalletPayment payment) {
                Log.d("Dapp", payment.toString());
            }

            @Override
            public void onError(DappException exception) {
                Log.d("Dapp", exception.getMessage(), exception);
            }
        });
    }
} catch (DappException e) {
    e.printStackTrace();
}

```

### Comprobar que es un QR Codi válido

1. Invoque las funcionalidades de validar código Codi:

```java
                 
try {
    String code = "{"TYP":20,"v":{"DEV":"00000161803561217910/0"}}";
    if (DappWallet.isCodi(code)){
        //do something with valid codi
    }else{
        //do somethihg with invalid codi
    }
} catch (DappException e) {
    e.printStackTrace();
}

```

### Identificar el tipo de QR

1. Invoque las funcionalidades de identificar un código QR. **Dapp Wallet** devolverá un objeto **DappQRType**.

```java
                 
try {
    String code = "{"TYP":20,"v":{"DEV":"00000161803561217910/0"}}";
    switch(DappWallet.getQRType(code)){
        case DappQRType.DAPP:
            //do Dapp stuff
            break;
        case DappQRType.CODI:
            //do Codi stuff
            break;
        case DappQRType.CODI_DAPP:
            //do Dapp-Codi stuff
            break;
        case DappQRType.UNKNOWN:
            //do Unknown stuff
            break;
    }
} catch (DappException e) {
    e.printStackTrace();
}

```

### Invocar el lector de códigos QR de **Dapp Wallet SDK**

1. Para poder usar el lector de codigos QR integrado en **Dapp Wallet SDK** es necesario agregar las siguientes dependencias al gradle de su proyecto:

```java

dependencies {
    implementation 'me.dm7.barcodescanner:zxing:1.9.8'
    implementation 'com.google.zxing:core:3.3.2'
}

```

2. Al ejecutar el metodo *DappWallet.dappReader* se iniciara una activity nueva mostrando el scanner de códigos, el call back recibe los datos del código en caso de que sea valido o un error en caso contrario:

```java

DappWallet.dappReader(new DappWalletCallback() {
    @Override
    public void onSuccess(DappWalletPayment payment) {
        Log.d("Dapp", payment.toString());
    }

    @Override
    public void onError(DappException exception) {
        Log.d("Dapp", exception.getMessage(), exception);
    }
});

```

3. En el método *onActivityResult* devuelva el resultado a **Dapp Wallet SDK**:

```java

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    DappWallet.onReaderResult(requestCode, resultCode, data);
}

```

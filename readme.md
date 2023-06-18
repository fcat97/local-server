# Local Host Library

A local server library to load web projects into `android web view`. 

__How To Use?__

- add jitpack to your project
  
  ```groovy
  // in project's root build.gradle
  allprojects {
      repositories {
  		// ...
  		maven { url 'https://jitpack.io' }
  	}
  }
  
  
  // or in settings.gradle file
  dependencyResolutionManagement {
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
      repositories {
          google()
          mavenCentral()
          maven { url "https://jitpack.io" }
      }
  }
  ```



- Then import the library. _tag_:[![](https://jitpack.io/v/fcat97/local-server.svg)](https://jitpack.io/#fcat97/local-server)

  ```groovy
  dependencies {
      implementation 'com.github.fcat97:local-server:Tag'
  }
  ```



Now to use,

1. Start the server & Set the web project's root path (where the `index.html` file is):
   
   ```kotlin
   ServerProvider.startServer()
   ServerProvider.setDistDir(distHelper.getDistPath(distName))
   ```

2.  Now just load the local host root i.e. `http://127.0.0.1:9099` in `WebView`. 

    ```kotlin
    if (ServerProvider.currentState is ServerState.Running) {
      val url = (ServerProvider.currentState as ServerState.Running).url
      webView.loadUrl(url)
    }
    ```

3. Remember to enable `javascript` and `domStorage`
   
   ```kotlin
   webView.apply {
       @SuppressLint("SetJavaScriptEnabled")
       settings.javaScriptEnabled = true
       settings.domStorageEnabled = true
   
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           webViewClient = this@DistViewActivity.webViewClient
       }
   }
   ```
   
   All done. 

---

Happy coding 🚀

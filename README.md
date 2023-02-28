# Content Queries (CONQUER) Attack 

This repository contains the artifacts of our [NDSS'23](https://www.ndss-symposium.org/wp-content/uploads/2023/02/ndss2023_f5_paper.pdf) paper titled ***Do Not Give a Dog Bread Every Time He Wags His Tail: Stealing Passwords through Content Queries (CONQUER) Attack***. Please refer to the paper for more details about the attack.


## Demos
You can build the demo using Android Studio.

### The Victim
You can find a sample victim app in the [LoginDemo](./LoginDemo) directory. Once you've launched the victim app, you will see four password input boxes: 
* `Password`: This is the normal password input box
* `Password with Content Description`: This is the password input box with a content description "pwd".
* `Password with Defense`: This is the password input box that never sends `AccessibilityEvent`s
* `Secure Password`: This is the secure password input box that is not affected by CONQUER.

Once you've installed and configured the malware, enter something into one of these password input boxes and click the `SIGN IN OR REGISTER` button, you should be able to see the output in the console of the malware app.

### The Malware
You can find the malware code in the [Conquer](./Conquer) directory. Once you've installed the malware, please grant it with the accessibility service permission in the system settings. After this, the malware should function normally.

### Attack Scenarios
1. **Normal**: Input something into the `Password`, and you'll see the case-insensitive version of the string and input time intervals.

2. **Active Query**: Set `do_active_query = true` in the source code (`Conquer\app\src\main\java\com\example\conquer\Conquer.java`) and re-build the malware (don't forget to grant the permission). Input something into the `Password with Defense`, and you'll see the output.

3. **Lazy Query**: Set `do_lazy_query = true` in the source code (`Conquer\app\src\main\java\com\example\conquer\Conquer.java`) and re-build the malware (don't forget to grant the permission). Input something into the `Password with Content Description`, and you'll see the output.

4. **Secure**: Input something into the `Secure Password`, and you'll see nothing :)

**P.S.**: In scenarios 2 and 3, you'll need to know the view id or content description of the password input box in advance. We recommend to use `adb shell uiautomator dump` to dump the UI hierarchy information of the login window, and you should be able to find the information you need.

## Password Recovery Method
Coming soon ...

## Scanning Framework
Coming soon...

## Citing our paper
```
@inproceedings{Lei::NDSS2023::CONQUER,
  title={Do Not Give a Dog Bread Every Time He Wags His Tail: Stealing Passwords through Content Queries ({CONQUER}) Attack},
  author={Chongqing Lei and Zhen Ling and Yue Zhang and Kai Dong and Kaizheng Liu and Junzhou Luo and Xinwen Fu},
  booktitle={Proceedings of the 2023 Network and Distributed System Security Symposium (NDSS)},
  year={2023}
}
```

## Contact
Open an issue if you have any questions.

Or you can contact the first author of the paper through email: leicq@seu.edu.cn
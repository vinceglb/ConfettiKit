//
//  ContentView.swift
//  ConfettiKitSample
//
//  Created by Vincent Guillebaud on 18/01/2025.
//

import UIKit
import SwiftUI
import SampleSharedKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.all)
    }
}

#Preview {
    ContentView()
}

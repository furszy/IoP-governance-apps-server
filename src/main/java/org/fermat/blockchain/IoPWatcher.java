//package org.fermat.blockchain;
//
//import com.sun.istack.internal.Nullable;
//import org.bitcoinj.core.*;
//import org.bitcoinj.core.listeners.PeerConnectedEventListener;
//import org.bitcoinj.core.listeners.PeerDataEventListener;
//import org.bitcoinj.params.RegTestParams;
//import org.bitcoinj.store.BlockStore;
//import org.bitcoinj.store.BlockStoreException;
//import org.bitcoinj.store.SPVBlockStore;
//import org.bitcoinj.wallet.Wallet;
//import org.bitcoinj.wallet.WalletProtobufSerializer;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.net.InetSocketAddress;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by mati on 24/02/17.
// */
//public class IoPWatcher {
//
//
//    private Wallet wallet;
//
//    private NetworkParameters params = RegTestParams.get();
//    private Context CONTEXT = Context.getOrCreate(params);
//
//    public void connect(){
//
//        Context.propagate(CONTEXT);
//
//        try {
//
//            File file = new File("wallet.dat");
//
//            if (!file.exists()) {
//                file.createNewFile();
//                wallet = new Wallet(params);
//            }else {
//                FileInputStream walletStream = new FileInputStream(file);
//                wallet = new WalletProtobufSerializer().readWallet(walletStream);
//                walletStream.close();
//            }
//
//            wallet.autosaveToFile(file,5, TimeUnit.SECONDS,null);
//
//            File blockStoreFile = new File("blockstore.dat");
//
//            BlockStore blockStore = new SPVBlockStore(params,blockStoreFile);
//
//            BlockChain blockChain = new BlockChain(params,wallet,blockStore);
//
//            final PeerGroup peerGroup = new PeerGroup(params,blockChain);
//
//            //regtest
//            peerGroup.addAddress(new PeerAddress(params,new InetSocketAddress("localhost",7685)));
//            // testnet
////            peerGroup.addAddress(new PeerAddress(params,new InetSocketAddress("192.168.0.111",7475)));
//
//            peerGroup.addConnectedEventListener(new PeerConnectedEventListener() {
//                @Override
//                public void onPeerConnected(Peer peer, int i) {
//                    System.out.println("onPeerConnected: "+peer);
//
//
//                }
//            });
//
//            peerGroup.start();
//            peerGroup.startBlockChainDownload(new PeerDataEventListener() {
//                @Override
//                public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
//                    System.out.println("onBlocksDownloaded: "+blocksLeft);
//
//                }
//
//                @Override
//                public void onChainDownloadStarted(Peer peer, int blocksLeft) {
//                    System.out.println("onChainDownloadStarted: number "+blocksLeft);
//                }
//
//                @Nullable
//                @Override
//                public List<Message> getData(Peer peer, GetDataMessage m) {
//                    return null;
//                }
//
//                @Override
//                public Message onPreMessageReceived(Peer peer, Message m) {
//                    return m;
//                }
//            });
//
//
//
//
//        } catch (BlockStoreException e) {
//            e.printStackTrace();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }
//
//}

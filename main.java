package test;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.*;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import org.json.JSONObject;

import java.util.List;

/**
 @author xiaodong
 @version v0.0.1
 @date 2021/7/6 10:15
 */
public class StsSessionToken2 {

    public static void main(String[] args) throws Exception {
        getSessionToken();
    }

    // STS创建对AWS资源的访问的临时安全凭证
    public static void getSessionToken() {

        try {
            int i = 0;
            while(i++ < 1000000) {
                ClientConfiguration config = new ClientConfiguration();
                config.setProtocol(Protocol.HTTPS);
                //config.setSignerOverride("S3SignerType");
                config.setSignerOverride("AWSS3V4SignerType");
                AWSCredentials credentials = new BasicAWSCredentials("luhang", "123456");
                AWSSecurityTokenService sts_client = AWSSecurityTokenServiceClientBuilder.standard()
                        .withClientConfiguration(config)
                        .withCredentials(new AWSStaticCredentialsProvider(credentials))
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://10.16.11.66:8000", "us-east-1"))
                        .build();

                GetSessionTokenRequest session_token_request = new GetSessionTokenRequest();
                session_token_request.setDurationSeconds(7200); // optional.

                GetSessionTokenResult session_token_result = sts_client.getSessionToken(session_token_request);
                Credentials session_creds = session_token_result.getCredentials();

                BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
                        session_creds.getAccessKeyId(),
                        session_creds.getSecretAccessKey(),
                        session_creds.getSessionToken());
                JSONObject jsonobj = new JSONObject(sessionCredentials);
                System.out.println("credentials: " + jsonobj);

                ClientConfiguration config1 = new ClientConfiguration();
                config1.setSignerOverride("S3SignerType");
                config.setProtocol(Protocol.HTTP);
                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://10.255.0.183:8000", ""))
                        .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
                        .withClientConfiguration(config1)
                        .withPathStyleAccessEnabled(true)
                        .build();
            }
            // 列出所有存储桶
            // listBuckets(s3Client);

            // 创建一个存储桶
            /*Bucket b = createBucket(s3Client, "bucket-xiaodong");
            if (b == null) {
                System.out.println("Error creating bucket!\n");
            } else {
                System.out.println("Done!\n");
            }*/

        }catch(AmazonServiceException ase) {
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        }catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    // 列出所有存储桶
    private static void listBuckets(AmazonS3 s3) {
        try {
            List<Bucket> buckets = s3.listBuckets();
            System.out.println("Your Amazon S3 buckets are:");
            for (Bucket b : buckets) {
                System.out.println("* " + b.getName());
            }
        }catch(AmazonServiceException ase) {
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        }catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    // 指定桶名获取存储桶.
    private static Bucket getBucket(AmazonS3 s3Client, String bucket_name) {
        //final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        Bucket named_bucket = null;
        List<Bucket> buckets = s3Client.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucket_name)) {
                named_bucket = b;
            }
        }
        return named_bucket;
    }

    // 创建存储桶.
    private static Bucket createBucket(AmazonS3 s3Client, String bucket_name) {
        //  final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
        Bucket b = null;
        if (s3Client.doesBucketExistV2(bucket_name)) {
            System.out.format("\nCannot create the bucket. \n" +
                    "A bucket named '%s' already exists.", bucket_name);
            b = getBucket(s3Client, bucket_name);
        } else {
            try {
                System.out.format("\nCreating a new bucket named '%s'...\n\n", bucket_name);
                b = s3Client.createBucket(bucket_name);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }
        return b;
    }
}
